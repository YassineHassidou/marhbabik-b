package com.marhababik360.gateway.mapper;

import com.marhababik360.gateway.dto.UserClaims;

import java.net.http.HttpRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class HeaderMapper {
    private HeaderMapper() {}
    public static void addUserHeaders(HttpRequest.Builder builder, UserClaims claims) {
        builder.header("X-User-Id", claims.userId);
        builder.header("X-User-Role", claims.role);
        if (claims.email != null) builder.header("X-User-Email", claims.email);
        if (claims.fullName != null) builder.header("X-User-FullName", encodeHeader(claims.fullName));
    }

    private static String encodeHeader(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
