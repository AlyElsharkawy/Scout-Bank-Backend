package org.sportingscout.scout_bank_backend.security;

public class Permissions {
  // Article permissions
  public static final String ARTICLE_VIEW = "article:view";
  public static final String ARTICLE_WRITE = "article:write";
  public static final String ARTICLE_EDIT = "article:edit";
  public static final String ARTICLE_DELETE = "article:delete";
  public static final String ARTICLE_REVIEW = "article:review";

  // User permissions
  public static final String USER_VIEW = "user:view";
  public static final String USER_DELETE = "user:delete";
  public static final String USER_ROLE_EDIT = "user:role-edit";

  // Organization permissions
  public static final String ORGANIZATION_VIEW = "organization:view";
  public static final String ORGANIZATION_CREATE = "organization:create";
  public static final String ORGANIZATION_EDIT = "organization:edit";

  // Media permissions
  public static final String MEDIA_UPLOAD = "media:upload";
  public static final String MEDIA_DELETE = "media:delete";
}
