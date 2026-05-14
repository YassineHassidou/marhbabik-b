package com.marhababik360.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.marhababik360.auth.config.JwtConfig;
import com.marhababik360.auth.dto.AuthResponse;
import com.marhababik360.auth.dto.LoginRequest;
import com.marhababik360.auth.dto.RegisterRequest;
import com.marhababik360.auth.exception.ApiException;
import com.marhababik360.auth.mapper.UserMapper;
import com.marhababik360.auth.model.User;
import com.marhababik360.auth.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegister(request);
        String now = Instant.now().toString();
        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.fullName = request.fullName.trim();
        user.email = normalizeEmail(request.email);
        user.passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(12));
        user.role = request.role.trim().toLowerCase(Locale.ROOT);
        user.age = request.age;
        user.cn = trimToNull(request.cn);
        user.phone = trimToNull(request.phone);
        user.category = trimToNull(request.category);
        user.businessName = trimToNull(request.businessName);
        user.profilePhoto = trimToNull(request.profilePhoto);
        user.createdAt = now;
        user.updatedAt = now;
        User saved = userRepository.save(user);
        return buildResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || isBlank(request.email) || isBlank(request.password)) {
            throw new ApiException(400, "Bad Request", "Email and password are required");
        }
        User user = userRepository.findByEmail(normalizeEmail(request.email))
                .orElseThrow(() -> new ApiException(401, "Unauthorized", "Invalid email or password"));
        if (!BCrypt.checkpw(request.password, user.passwordHash)) {
            throw new ApiException(401, "Unauthorized", "Invalid email or password");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        long expiration = JwtConfig.expirationSeconds();
        Instant now = Instant.now();
        String token = JWT.create()
                .withIssuer(JwtConfig.ISSUER)
                .withSubject(user.id)
                .withClaim("role", user.role)
                .withClaim("email", user.email)
                .withClaim("fullName", user.fullName)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(expiration)))
                .sign(Algorithm.HMAC256(JwtConfig.secret()));

        AuthResponse response = new AuthResponse();
        response.accessToken = token;
        response.expiresIn = expiration;
        response.user = UserMapper.toResponse(user);
        return response;
    }

    private void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new ApiException(400, "Bad Request", "Request body is required");
        }
        if (isBlank(request.fullName)) {
            throw new ApiException(400, "Bad Request", "fullName is required");
        }
        if (isBlank(request.email)) {
            throw new ApiException(400, "Bad Request", "email is required");
        }
        if (isBlank(request.password) || request.password.length() < 8) {
            throw new ApiException(400, "Bad Request", "password must contain at least 8 characters");
        }
        if (isBlank(request.role)) {
            throw new ApiException(400, "Bad Request", "role is required");
        }
        String role = request.role.trim().toLowerCase(Locale.ROOT);
        if (!role.equals("visitor") && !role.equals("worker")) {
            throw new ApiException(400, "Bad Request", "role must be visitor or worker");
        }
        if (role.equals("worker") && isBlank(request.category)) {
            throw new ApiException(400, "Bad Request", "category is required for workers");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }
}
