package com.marhababik360.user.service;

import com.marhababik360.user.dto.UpdateUserRequest;
import com.marhababik360.user.dto.UserContext;
import com.marhababik360.user.dto.UserResponse;
import com.marhababik360.user.exception.ApiException;
import com.marhababik360.user.mapper.UserMapper;
import com.marhababik360.user.model.User;
import com.marhababik360.user.repository.UserRepository;

import java.time.Instant;

public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public UserResponse getOrCreateProfile(UserContext context) {
        return UserMapper.toResponse(userRepository.findById(context.userId).orElseGet(() -> createFromContext(context)));
    }

    public UserResponse getById(UserContext context, String id) {
        if (isBlank(id)) throw new ApiException(400, "Bad Request", "User id is required");
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(404, "Not Found", "User not found"));
        return UserMapper.toResponse(user);
    }

    public UserResponse updateProfile(UserContext context, UpdateUserRequest request) {
        if (request == null) throw new ApiException(400, "Bad Request", "Request body is required");
        User user = userRepository.findById(context.userId).orElseGet(() -> createFromContext(context));
        if (!isBlank(request.fullName)) user.fullName = request.fullName.trim();
        user.age = request.age;
        user.cn = trimToNull(request.cn);
        user.phone = trimToNull(request.phone);
        user.category = trimToNull(request.category);
        user.businessName = trimToNull(request.businessName);
        user.profilePhoto = trimToNull(request.profilePhoto);
        user.updatedAt = Instant.now().toString();
        return UserMapper.toResponse(userRepository.update(user));
    }

    private User createFromContext(UserContext context) {
        String now = Instant.now().toString();
        User user = new User();
        user.id = context.userId;
        user.fullName = isBlank(context.fullName) ? "User " + context.userId.substring(0, Math.min(8, context.userId.length())) : context.fullName;
        user.email = context.email;
        user.role = context.role;
        user.createdAt = now;
        user.updatedAt = now;
        return userRepository.save(user);
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private String trimToNull(String value) { return isBlank(value) ? null : value.trim(); }
}
