package org.rookedsysc.oauthoidc.constant;

import java.util.List;

public class SecurityConstant {
    public static final List<String> SWAGGER = List.of(
        "/v2/api-docs",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/api-docs/**"
    );
    public static final List<String> EXCLUDE_URL = List.of(
        "/user/**"
    );
    public static String AUTHORIZATION_HEADER = "Authorization";
    public static String BEARER_PREFIX = "Bearer ";
    public static String CLAIM_KEY_USER_ID = "userId";
    public static String CLAIM_KEY_ID_TYPE = "idType";

}
