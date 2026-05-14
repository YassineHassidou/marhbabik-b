package com.marhababik360.category.mapper;

import com.marhababik360.category.dto.CategoryResponse;
import com.marhababik360.category.model.Category;

public final class CategoryMapper {
    private CategoryMapper() {}
    public static CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.id = category.id;
        response.name = category.name;
        response.icon = category.icon;
        return response;
    }
}
