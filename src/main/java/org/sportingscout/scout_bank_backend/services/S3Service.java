package org.sportingscout.scout_bank_backend.services;

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
import software.amazon.awssdk.awscore.presigner.PresignedRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import java.util.stream.Collectors;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class S3Service {
  private final S3Client client;
  private final String bucketName;
  private final S3Presigner presigner;
  private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

  public S3Service(S3Client client, @Value("${s3.bucket-name}") String bucketName,
      S3Presigner presigner) {
    this.client = client;
    this.bucketName = bucketName;
    this.presigner = presigner;
  }

  public void uploadFile(String keyName, MultipartFile multiPartFile) {
    try {
      InputStream inputStream = multiPartFile.getInputStream();
      long contentLength = multiPartFile.getSize();
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(this.bucketName)
          .contentType(multiPartFile.getContentType())
          .key(keyName)
          .build();

      this.client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
      logger.debug("Successfully uploaded {} to S3", keyName);
    } catch (S3Exception e) {
      logger.error("S3 Server Error - Uploading {}: {} | Status: {}", keyName, e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the file", e);
    } catch (SdkException e) {
      logger.error("Network Error - Uploading | Could not reach S3: {}", keyName, e.getMessage());
      throw new RuntimeException("Failed to connect to S3", e);
    } catch (IOException e) {
      // Fails if the client disconnects mid-upload or the internal buffer breaks
      logger.error("Failed to read bytes from the incoming multipart request for key: {}", keyName, e);
      throw new RuntimeException("Failed to read upload stream", e);
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
