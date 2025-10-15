package ru.practicum.service.category;



import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryNewDto;
import ru.practicum.dto.category.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {

    CategoryDto get(Long id);

    List<CategoryDto> get(List<Long> ids);

    List<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto create(CategoryNewDto dto);

    CategoryDto update(Long id, CategoryUpdateDto dto);

    void delete(Long id);

    boolean existsById(Long id);
}
