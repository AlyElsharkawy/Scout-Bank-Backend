package org.sportingscout.scout_bank_backend.services;

import org.sportingscout.scout_bank_backend.security.PermissionsConfig;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionMediaRepository;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionMedia;
import org.sportingscout.scout_bank_backend.utils.S3FileUtilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.io.IOException;

import java.time.Duration;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;

@Service
public class S3Service {
  private final S3Client client;
  private final String bucketName;
  private final S3Presigner presigner;
  private final PermissionsConfig permissionsConfig;
  private final ArticleVersionMediaRepository articleVersionMediaRepository;
  private final UsersRepository usersRepository;
  private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

  public S3Service(S3Client client, @Value("${s3.bucket-name}") String bucketName,
      S3Presigner presigner, PermissionsConfig permissionsConfig,
      ArticleVersionMediaRepository articleVersionMediaRepository,
      UsersRepository usersRepository) {
    this.client = client;
    this.bucketName = bucketName;
    this.presigner = presigner;
    this.permissionsConfig = permissionsConfig;
    this.articleVersionMediaRepository = articleVersionMediaRepository;
    this.usersRepository = usersRepository;
  }

  @Transactional
  private Long saveArticleMediaRelationshipToDB(ArticleVersionMedia relationship) {
    return articleVersionMediaRepository.save(relationship).getId();
  }

  @Transactional
  private Long saveProfilePictureToDB(ApplicationUser user) {
    return usersRepository.save(user).getId();
  }

  // TODO: Add file type determination based on the file, rather than its name
  public String uploadArticleFile(String fileName, MultipartFile multiPartFile) {
    S3FileUtilities.validateFileName(fileName, S3FileUtilities.ALLOWED_MEDIA_EXTENSIONS);
    try {
      ApplicationUser user = permissionsConfig.getAuthenticatedUser();
      InputStream inputStream = multiPartFile.getInputStream();
      Long contentLength = multiPartFile.getSize();
      String keyName = S3FileUtilities.getArticleKeyName(
          user.getOrganization().getInternalName(), fileName);
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(this.bucketName)
          .contentType(multiPartFile.getContentType())
          .key(keyName)
          .build();

      this.client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
      logger.debug("Successfully uploaded media {} to S3", fileName);

      // After storing the image with the UUID name, store its original name in the
      // database

      ArticleVersionMedia relationship = ArticleVersionMedia.builder()
          .uploadedBy(user)
          .fileName(fileName)
          .key(keyName)
          .mimeType(multiPartFile.getContentType())
          .build();

      try {
        try {
          saveArticleMediaRelationshipToDB(relationship);
        } catch (Exception deletionException) {
          // TODO: Add an outbox for pending orphaned S3 object deletion requests
          logger.error("Failed to delete orphaned S3 article media object {}: {}", keyName,
              deletionException.getMessage());
        }
      } catch (Exception e) {
        deleteFile(keyName);
        logger.warn("Flushing image metadata to PostgreSQL database has failed. S3 upload has been reversed");
        throw new RuntimeException(
            "Flushing image metadata to PostgreSQL database has failed. S3 upload has been reversed");
      }

      return keyName;
    } catch (S3Exception e) {
      logger.error("S3 Server Error - Uploading Media {}: {} | Status: {}", fileName,
          e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the file during media upload", e);
    } catch (SdkException e) {
      logger.error("Network Error - Uploading Media | Could not reach S3: {}", fileName, e.getMessage());
      throw new RuntimeException("Failed to connect to S3 during media upload", e);
    } catch (IOException e) {
      // Fails if the client disconnects mid-upload or the internal buffer breaks
      logger.error("Failed to read bytes from the incoming multipart request for key during media upload: {}", fileName,
          e);
      throw new RuntimeException("Failed to read upload stream", e);
    } catch (Exception e) {
      logger.error("Unknown S3 error while uploading media: " + e.getMessage());
      throw new RuntimeException("Unknown S3 error while uploading media", e);
    }
  }

  public String uploadProfilePicture(String fileName, MultipartFile multiPartFile) {
    S3FileUtilities.validateFileName(fileName, S3FileUtilities.ALLOWED_PROFILE_EXTENSIONS);
    try {
      ApplicationUser user = permissionsConfig.getAuthenticatedUser();
      InputStream inputStream = multiPartFile.getInputStream();
      Long contentLength = multiPartFile.getSize();
      String keyName = S3FileUtilities.getUserKeyName(user.getId().toString(), fileName);
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(this.bucketName)
          .contentType(multiPartFile.getContentType())
          .key(keyName)
          .build();

      this.client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
      logger.debug("Successfully uploaded profile picture {} to S3", fileName);

      user.setProfilePicture(keyName);

      try {
        try {
          saveProfilePictureToDB(user);
        } catch (Exception deletionException) {
          // TODO: Add an outbox for pending orphaned S3 object deletion requests
          logger.error("Failed to delete orphaned S3 profile picture {}: {}", keyName, deletionException.getMessage());
        }
      } catch (Exception e) {
        deleteFile(keyName);
        logger.warn("Flushing image metadata to PostgreSQL database has failed. S3 upload has been reversed");
        throw new RuntimeException(
            "Flushing image metadata to PostgreSQL database has failed. S3 upload has been reversed");
      }

      return keyName;
    } catch (S3Exception e) {
      logger.error("S3 Server Error - Uploading Profile Picture {}: {} | Status: {}", fileName,
          e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the file during profile picture upload", e);
    } catch (SdkException e) {
      logger.error("Network Error - Uploading Profile Picture | Could not reach S3: {}", fileName, e.getMessage());
      throw new RuntimeException("Failed to connect to S3 during profile picture upload", e);
    } catch (IOException e) {
      // Fails if the client disconnects mid-upload or the internal buffer breaks
      logger.error("Failed to read bytes from the incoming multipart request for key during profile picture upload: {}",
          fileName,
          e);
      throw new RuntimeException("Failed to read upload stream", e);
    } catch (Exception e) {
      logger.error("Unknown S3 error while uploading profile picture: " + e.getMessage());
      throw new RuntimeException("Unknown S3 error while uploading profile picture", e);
    }
  }

  public List<String> listFiles(String prefix) {
    try {
      ListObjectsV2Request req = ListObjectsV2Request.builder()
          .bucket(this.bucketName)
          .prefix(prefix)
          .build();

      ListObjectsV2Response response = client.listObjectsV2(req);
      logger.debug("Listed contents of {}", prefix);
      return response
          .contents()
          .stream()
          .map(S3Object::key)
          .collect(Collectors.toList());

    } catch (S3Exception e) {
      logger.error("S3 Server Error - Listing {}: {} | Status: {}", prefix, e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the listing", e);
    } catch (SdkException e) {
      logger.error("Network Error - Listing {}: | Could not reach S3: {}", prefix, e.getMessage());
      throw new RuntimeException("Failed to connect to S3", e);
    }
  }

  public void deleteFile(String keyName) {
    try {
      DeleteObjectRequest req = DeleteObjectRequest.builder()
          .bucket(this.bucketName)
          .key(keyName)
          .build();

      this.client.deleteObject(req);
      logger.debug("Deleted {} from S3", keyName);
    } catch (S3Exception e) {
      logger.error("S3 Server Error - Listing {}: {} | Status: {}", keyName, e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the deletion of {}", e);
    } catch (SdkException e) {
      logger.error("Network Error - Deletion: {} | Could not reach S3: {}", keyName, e.getMessage());
      throw new RuntimeException("Failed to connect to S3", e);
    }
  }

  public byte[] downloadFileBytes(String keyName) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(keyName)
          .build();

      // ResponseInputStream is a specialized stream that includes S3 metadata
      ResponseInputStream<GetObjectResponse> response = client.getObject(getObjectRequest);

      byte[] content = response.readAllBytes();
      logger.info("Downloaded {} successfully from S3", keyName);
      return content;

    } catch (Exception e) {
      logger.error("Failed to download file {] from S3: {}", keyName, e.getMessage());
      throw new RuntimeException("Could not retrieve file", e);
    }
  }

  public String getPresignedUrl(String keyName) {
    try {
      GetObjectRequest objectRequest = GetObjectRequest.builder()
          .bucket(this.bucketName)
          .key(keyName)
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(2))
          .getObjectRequest(objectRequest)
          .build();

      PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
      String urLResult = presignedRequest.url().toExternalForm();

      logger.debug("Generated presigned URL for key {} at {} from the following HTTP request {}", keyName, urLResult,
          presignedRequest.httpRequest().method());

      return urLResult;
    } catch (SdkException e) {
      logger.error("AWS SDK configuration error. Could not created presigned GET URL for key {}", keyName);
      throw new RuntimeException("Internal Storage Configuration Error");
    } catch (Exception e) {
      logger.error("Unknown error while creating presigned GET URL for key {}", keyName);
      throw new RuntimeException("Unknown Internal Storage Error");
    }
  }
}
