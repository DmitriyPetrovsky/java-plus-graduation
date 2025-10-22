package ru.practicum.controller.category;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryNewDto;
import ru.practicum.dto.category.CategoryUpdateDto;
import ru.practicum.service.category.CategoryService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CategoryNewDto newCategoryDto) {
        return categoryService.create(newCategoryDto);
    }

    @PatchMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@PathVariable @Positive Long categoryId,
                    @RequestBody @Valid CategoryUpdateDto categoryDto) {
        return categoryService.update(categoryId, categoryDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long categoryId) {
        categoryService.delete(categoryId);
    }
}
