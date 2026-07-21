package org.sportingscout.scout_bank_backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

import org.sportingscout.scout_bank_backend.services.S3Service;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class S3Controller {
  private final S3Service service;

  public S3Controller(S3Service service) {
    this.service = service;
  }

  @PreAuthorize("@auth.has('media:upload')")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("fileName") String fileName) {

    String keyName = service.uploadArticleFile(fileName, file);
    return ResponseEntity.ok(keyName);
  }

  @PutMapping("/overwrite")
  public ResponseEntity<Void> overwriteFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("key") String key) {
    service.uploadArticleFile(key, file);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping
  public ResponseEntity<List<String>> listFiles(
      @RequestParam(name = "prefix", required = false, defaultValue = "") String prefix) {
    return ResponseEntity.ok(service.listFiles(prefix));
  }

  @DeleteMapping
  @PreAuthorize("@auth.has('media:delete')")
  public ResponseEntity<Void> deleteFile(
      @RequestParam(name = "key", required = false, defaultValue = "") String key) {
    service.deleteFile(key);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/url")
  public ResponseEntity<String> getPresignedUrl(
      @RequestParam(name = "key", required = true) String keyName) {
    return ResponseEntity.ok(service.getPresignedUrl(keyName));
  }
}
