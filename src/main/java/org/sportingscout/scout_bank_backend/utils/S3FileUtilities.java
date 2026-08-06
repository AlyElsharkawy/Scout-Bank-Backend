package org.sportingscout.scout_bank_backend.utils;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import java.util.Set;
import java.util.Optional;

import java.time.LocalDate;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;

public final class S3FileUtilities {
  public static final Set<String> ALLOWED_MEDIA_EXTENSIONS = Set.of(
      "MP4", "MKV", "JPG", "JPEG", "PNG", "GIF");

  public static final Set<String> ALLOWED_PROFILE_EXTENSIONS = Set.of(
      "PNG", "JPG", "JPEG");

  public static void validateFileName(String fileName, Set<String> validationSet) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }
    if (!fileName.chars().allMatch(ch -> ch < 128)) {
      throw new IllegalArgumentException("File name can only contain standard English characters");
    }
    if (!hasValidExtension(fileName)) {
      throw new IllegalArgumentException(
          "File type is unsupported. Only " + validationSet.toString() + " is supported");
    }
  }

  private S3FileUtilities() {
  }

  public static Optional<String> getFileExtension(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return Optional.empty();
    }
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
      return Optional.empty();
    }

    return Optional.of(fileName.substring(lastDotIndex + 1));
  }

  public static boolean hasValidExtension(String fileName) {
    return getFileExtension(fileName)
        .map(ext -> ALLOWED_MEDIA_EXTENSIONS.contains(ext.toUpperCase()))
        .orElse(false);
  }

  public static String getArticleKeyName(String organizationName, String fileName) {
    // Example:
    // /articles/alexandria-sporting-club/2026/07/26/084279d1-b5d5-4514-9a0f-d7fcd7181f94.png
    String dateSection = LocalDate.now().getYear() + "/" +
        LocalDate.now().getMonthValue() + "/" +
        LocalDate.now().getDayOfMonth();

    String finalKey = "articles/" +
        organizationName + "/" + dateSection + "/" +
        UuidCreator.getTimeBased() + "." +
        getFileExtension(fileName).get();
    return finalKey;
  }

  public static String getUserKeyName(String userId, String fileName) {
    // Example:
    // /users/084279d1-b5d5-4514-9a0f-d7fcd7181f94/profile-098bcf.png
    // I won't add the random chars at the end now
    String result = "users/" + userId + "/profile." + getFileExtension(fileName).get();
    return result;
  }
}
