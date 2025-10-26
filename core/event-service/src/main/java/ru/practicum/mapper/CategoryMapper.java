package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryNewDto;
import ru.practicum.dto.category.CategoryUpdateDto;
import ru.practicum.model.category.Category;


import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    Category fromDto(CategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    Category fromDto(CategoryNewDto categoryDto);

    Category fromDto(CategoryUpdateDto categoryDto);

    List<Category> fromDto(List<CategoryDto> categoryDtos);

    List<CategoryDto> toDto(List<Category> categories);
}