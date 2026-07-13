package com.example.library.mapper;

import com.example.library.domain.Category;
import com.example.library.dto.response.CategoryResponse;
import com.example.library.dto.response.SimpleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
