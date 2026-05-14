package com.marhababik360.gateway.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.marhababik360.gateway.config.JwtConfig;
import com.marhababik360.gateway.dto.UserClaims;
import com.marhababik360.gateway.exception.ApiException;
import com.sun.net.httpserver.HttpExchange;

public class JwtService {
    public UserClaims verify(HttpExchange exchange) {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "Unauthorized", "Bearer token is required");
        }
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(JwtConfig.secret())).withIssuer(JwtConfig.ISSUER).build();
            DecodedJWT jwt = verifier.verify(authorization.substring("Bearer ".length()));
            UserClaims claims = new UserClaims();
            claims.userId = jwt.getSubject();
            claims.role = jwt.getClaim("role").asString();
            claims.email = jwt.getClaim("email").asString();
            claims.fullName = jwt.getClaim("fullName").asString();
            if (claims.userId == null || claims.role == null) throw new ApiException(401, "Unauthorized", "Invalid token claims");
            return claims;
        } catch (JWTVerificationException exception) {
            throw new ApiException(401, "Unauthorized", "Invalid or expired token");
        }
    }
}
