package com.marhababik360.category.service;

import com.marhababik360.category.dto.CategoryResponse;
import com.marhababik360.category.mapper.CategoryMapper;
import com.marhababik360.category.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) { this.categoryRepository = categoryRepository; }
    public List<CategoryResponse> list() { return categoryRepository.findAll().stream().map(CategoryMapper::toResponse).toList(); }
}
