package org.sportingscout.scout_bank_backend.entities;

import java.io.Serializable;
import java.util.Objects;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVersionMediaId implements Serializable {

  private Long articleVersion;
  private Long media;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ArticleVersionMediaId that = (ArticleVersionMediaId) o;
    return Objects.equals(articleVersion, that.articleVersion) &&
        Objects.equals(media, that.media);
  }

  @Override
  public int hashCode() {
    return Objects.hash(articleVersion, media);
  }
}
