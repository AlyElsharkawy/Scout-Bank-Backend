package org.sportingscout.scout_bank_backend.dtos.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersion;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

public record ArticleVersionWithMedia(
    ArticleVersion articleVersion,
    List<Media> media) {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PUBLIC)
  public static class Media {
    String key;
    String caption;
    String fileName;
    String mimeType;
  }
}
