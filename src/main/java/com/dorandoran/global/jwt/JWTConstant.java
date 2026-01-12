package com.dorandoran.global.jwt;

public abstract class JWTConstant {
    public static final String ACCESS_TOKEN_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    public static final String GUEST_TOKEN_HEADER = "GuestToken";
    public static final String ACCESS_TOKEN_PREFIX = "Bearer";

    public static final String CLAIM_KEY_USER_ID = "userId";
    public static final String CLAIM_KEY_USER_CATEGORY = "category";
    public static final String CLAIM_KEY_USER_ROLE = "role";

    public static final String ACCESS_TOKEN_CATEGORY = "accessToken";
    public static final String REFRESH_TOKEN_CATEGORY = "refreshToken";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final int GUEST_TOKEN_EXPIRE_SECONDS = 30 * 60; // 30 minutes
}
