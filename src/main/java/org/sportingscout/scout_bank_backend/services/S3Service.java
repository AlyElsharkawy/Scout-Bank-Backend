package org.sportingscout.scout_bank_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class S3Service {
  private final S3Client client;
  private final String bucketName;
  private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

  public S3Service(S3Client client, @Value("${s3.bucket-name}") String bucketName) {
    this.client = client;
    this.bucketName = bucketName;
  }

  public void uploadFile(String keyName, File file) {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(this.bucketName)
          .key(keyName)
          .build();

      this.client.putObject(request, RequestBody.fromFile(file));
      logger.debug("Successfully uploaded {} to S3", keyName);
    } catch (S3Exception e) {
      logger.error("S3 Server Error - Uploading {}: {} | Status: {}", keyName, e.awsErrorDetails().errorMessage(),
          e.statusCode());
      throw new RuntimeException("S3 rejected the file", e);
    } catch (SdkException e) {
      logger.error("Network Error - Uploading | Could not reach S3: {}", keyName, e.getMessage());
      throw new RuntimeException("Failed to connect to S3", e);
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
}
