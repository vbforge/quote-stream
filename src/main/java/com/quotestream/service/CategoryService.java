package com.quotestream.service;

import com.quotestream.dto.CategoryView;
import com.quotestream.model.Category;
import com.quotestream.model.User;
import com.quotestream.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllByOwner(User owner) {
        return categoryRepository.findByOwnerOrderByNameAsc(owner);
    }

    public List<CategoryView> getAllByOwnerWithCounts(User owner) {
        return categoryRepository.findByOwnerOrderByNameAsc(owner).stream()
                .map(cat -> CategoryView.of(cat, categoryRepository.countQuotesByCategory(cat)))
                .toList();
    }

    @Transactional
    public Category create(User owner, String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Category name cannot be empty");
        if (categoryRepository.existsByNameAndOwner(trimmed, owner)) {
            throw new IllegalArgumentException("Category already exists: " + trimmed);
        }
        Category category = Category.builder()
                .name(trimmed)
                .owner(owner)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category rename(User owner, Long categoryId, String newName) {
        Category category = categoryRepository.findByIdAndOwner(categoryId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        String trimmed = newName.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Category name cannot be empty");
        category.setName(trimmed);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(User owner, Long categoryId) {
        Category category = categoryRepository.findByIdAndOwner(categoryId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        categoryRepository.delete(category);
    }
}
