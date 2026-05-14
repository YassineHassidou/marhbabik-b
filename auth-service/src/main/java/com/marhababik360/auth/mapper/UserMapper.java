package com.marhababik360.auth.mapper;

import com.marhababik360.auth.dto.UserResponse;
import com.marhababik360.auth.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.id = user.id;
        response.fullName = user.fullName;
        response.email = user.email;
        response.role = user.role;
        response.age = user.age;
        response.cn = user.cn;
        response.phone = user.phone;
        response.category = user.category;
        response.businessName = user.businessName;
        response.profilePhoto = user.profilePhoto;
        response.createdAt = user.createdAt;
        response.updatedAt = user.updatedAt;
        return response;
    }
}
