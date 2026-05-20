package org.sportingscout.scout_bank_backend.dtos.articles;

import java.util.List;

public record CreateArticleVersionRequest(
    String title,
    String content,
    Long articleTypeId,
    List<Long> editorIds,
    int majorVersion,
    int minorVersion,
    List<Long> tagIds,
    List<String> imageKeys,
    List<String> videoKeys) {
}
