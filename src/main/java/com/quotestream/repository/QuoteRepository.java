package com.quotestream.repository;

import com.quotestream.model.Category;
import com.quotestream.model.Quote;
import com.quotestream.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    // Owner's quotes
    List<Quote> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<Quote> findByIdAndOwner(Long id, User owner);

    // Stream queries
    @Query("SELECT q FROM Quote q WHERE q.active = true AND q.publicVisible = true ORDER BY RANDOM()")
    List<Quote> findRandomPublicActive();

    @Query("SELECT q FROM Quote q WHERE q.owner = :owner AND q.active = true ORDER BY RANDOM()")
    List<Quote> findRandomActiveByOwner(@Param("owner") User owner);

    @Query("SELECT q FROM Quote q WHERE q.owner = :owner AND q.category = :category AND q.active = true ORDER BY RANDOM()")
    List<Quote> findRandomActiveByOwnerAndCategory(@Param("owner") User owner, @Param("category") Category category);

    @Query("SELECT q FROM Quote q WHERE q.active = true AND (q.publicVisible = true OR q.owner = :owner) ORDER BY RANDOM()")
    List<Quote> findRandomActiveMixed(@Param("owner") User owner);

    @Query("SELECT q FROM Quote q WHERE q.active = true AND (q.publicVisible = true OR q.owner = :owner) AND (:category IS NULL OR q.category = :category) ORDER BY RANDOM()")
    List<Quote> findRandomActiveMixedByCategory(@Param("owner") User owner, @Param("category") Category category);

    long countByOwner(User owner);
    long countByOwnerAndPublicVisible(User owner, boolean publicVisible);
    long countByOwnerAndActive(User owner, boolean active);
}
