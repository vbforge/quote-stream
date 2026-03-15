package com.quotestream.repository;

import com.quotestream.model.Category;
import com.quotestream.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwnerOrderByNameAsc(User owner);
    Optional<Category> findByIdAndOwner(Long id, User owner);
    boolean existsByNameAndOwner(String name, User owner);

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.category = :category")
    long countQuotesByCategory(@Param("category") Category category);
}
