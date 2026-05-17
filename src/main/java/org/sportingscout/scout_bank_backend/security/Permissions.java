package org.sportingscout.scout_bank_backend.security;

public class Permissions {
  // Article permissions
  public static final String ARTICLE_WRITE = "article:write";
  public static final String ARTICLE_EDIT = "article:edit";
  public static final String ARTICLE_DELETE = "article:delete";
  public static final String ARTICLE_REVIEW = "article:review";

  // Tag permissions
  public static final String TAG_CREATE = "tag:create";
  public static final String TAG_EDIT = "tag:edit";
  public static final String TAG_DELETE = "tag:delete";

  // User permissions
  public static final String USER_VIEW = "user:view";
  public static final String USER_DELETE = "user:delete";
  public static final String USER_ROLE_EDIT = "user:role-edit";
  public static final String USER_RANK_EDIT = "user:rank-edit";

  // Organization permissions
  public static final String ORGANIZATION_CREATE = "organization:create";
  public static final String ORGANIZATION_EDIT = "organization:edit";
  public static final String ORGANIZATION_DELETE = "organization:delete";

  // Media permissions
  public static final String MEDIA_UPLOAD = "media:upload";
  public static final String MEDIA_DELETE = "media:delete";

  // Rank permissions
  public static final String RANK_CREATE = "rank:create";
  public static final String RANK_EDIT = "rank:edit";
  public static final String RANK_DELETE = "rank:delete";

  // Role permissions
  public static final String ROLE_CREATE = "role:create";
  public static final String ROLE_EDIT = "role:edit";
  public static final String ROLE_DELETE = "role:delete";

  // Miscellaneous permissions
  public static final String DOCUMENTATION_ACCESS = "docs:access";
}
