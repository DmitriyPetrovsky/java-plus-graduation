package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.category.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> getByIdIn(List<Long> ids);

    Optional<Category> findByIdAndName(Long ids, String name);

    boolean existsByName(String name);

    Page<Category> findAll(Pageable pageable);
}
