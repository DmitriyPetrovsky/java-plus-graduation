package ru.practicum.service.category;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryNewDto;
import ru.practicum.dto.category.CategoryUpdateDto;
import ru.practicum.exception.BadConditionsException;
import ru.practicum.exception.DuplicatedDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.category.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.service.event.EventServiceHelper;


import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventServiceHelper eventServiceHelper;

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        int pageNumber = from / size;
        Pageable page = PageRequest.of(pageNumber, size);

        Page<Category> categoryPage = categoryRepository.findAll(page);
        System.out.println("Total elements: " + categoryPage.getTotalElements()); // дебаг
        System.out.println("Page size: " + categoryPage.getSize()); // дебаг
        System.out.println("Content size: " + categoryPage.getContent().size()); // дебаг

        return categoryPage.getContent().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto get(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с таким id не найдена"));
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> get(List<Long> ids) {
        return categoryMapper.toDto(categoryRepository.getByIdIn(ids));
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryNewDto dto) {
        Category category = categoryMapper.fromDto(dto);

        checkExistsByNameThrowError(dto.getName());

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с таким id не найдена"));

        Optional<Category> categorySameName = categoryRepository.findByIdAndName(id, dto.getName());

        if (categorySameName.isPresent()) {
            return categoryMapper.toDto(categorySameName.get());
        }

        checkExistsByNameThrowError(dto.getName());

        category.setName(dto.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория с таким id не найдена");
        }
        if (eventServiceHelper.checkEventsExistInCategory(id)) {
            throw new BadConditionsException("Нельзя удалить категорию с событиями");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    private void checkExistsByNameThrowError(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new DuplicatedDataException("Категория с таким именем уже существует");
        }
    }
}
