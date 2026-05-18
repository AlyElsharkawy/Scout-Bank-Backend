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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import org.sportingscout.scout_bank_backend.services.S3Service;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/media")
public class S3Controller {
  private final S3Service service;

  public S3Controller(S3Service service) {
    this.service = service;
  }

  // TODO: Ofcourse, we will format the string
  // That is to be done later though
  // I will prioritize basic functionality now
  @PreAuthorize("@auth.has('media:upload')")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("key") String key) {

    if (file.isEmpty() || key == null || key.isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    service.uploadFile(key, file);
    return ResponseEntity.ok(key);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleInternalError(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
}
