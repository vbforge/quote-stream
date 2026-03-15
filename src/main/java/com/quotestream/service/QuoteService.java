package com.quotestream.service;

import com.quotestream.dto.QuoteRequest;
import com.quotestream.model.*;
import com.quotestream.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CategoryRepository categoryRepository;
    private final StreamSettingsRepository streamSettingsRepository;

    // ── Stream logic ──────────────────────────────────────────────────────────

    /**
     * Returns the next quote for an anonymous user (random from all public active quotes).
     */
    public Optional<Quote> getNextForAnonymous() {
        List<Quote> quotes = quoteRepository.findRandomPublicActive();
        return quotes.isEmpty() ? Optional.empty() : Optional.of(quotes.get(0));
    }

    /**
     * Returns the next quote for a registered user based on their stream settings.
     */
    public Optional<Quote> getNextForUser(User user) {
        StreamSettings settings = streamSettingsRepository.findByUser(user)
                .orElse(defaultSettings(user));

        List<Quote> quotes = switch (settings.getSourceMode()) {
            case OWN -> {
                Category cat = settings.getCategory();
                if (cat != null) {
                    yield quoteRepository.findRandomActiveByOwnerAndCategory(user, cat);
                } else {
                    yield quoteRepository.findRandomActiveByOwner(user);
                }
            }
            case COMMUNITY -> quoteRepository.findRandomPublicActive();
            case MIXED -> {
                Category cat = settings.getCategory();
                if (cat != null) {
                    yield quoteRepository.findRandomActiveMixedByCategory(user, cat);
                } else {
                    yield quoteRepository.findRandomActiveMixed(user);
                }
            }
        };

        return quotes.isEmpty() ? Optional.empty() : Optional.of(quotes.get(0));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<Quote> getAllByOwner(User owner) {
        return quoteRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional
    public Quote create(User owner, QuoteRequest request) {
        Category category = resolveCategory(owner, request.getCategoryId());

        Quote quote = Quote.builder()
                .text(request.getText())
                .author(request.getAuthor())
                .publicVisible(request.isPublicVisible())
                .active(true)
                .owner(owner)
                .category(category)
                .build();

        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(User owner, Long quoteId, QuoteRequest request) {
        Quote quote = quoteRepository.findByIdAndOwner(quoteId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found or access denied"));

        Category category = resolveCategory(owner, request.getCategoryId());

        quote.setText(request.getText());
        quote.setAuthor(request.getAuthor());
        quote.setPublicVisible(request.isPublicVisible());
        quote.setCategory(category);

        return quoteRepository.save(quote);
    }

    @Transactional
    public void delete(User owner, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndOwner(quoteId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found or access denied"));
        quoteRepository.delete(quote);
    }

    @Transactional
    public void toggleActive(User owner, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndOwner(quoteId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found or access denied"));
        quote.setActive(!quote.isActive());
        quoteRepository.save(quote);
    }

    @Transactional
    public void togglePublic(User owner, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndOwner(quoteId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found or access denied"));
        quote.setPublicVisible(!quote.isPublicVisible());
        quoteRepository.save(quote);
    }

    public Quote getByIdAndOwner(Long id, User owner) {
        return quoteRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found or access denied"));
    }

    // Stats
    public long countByOwner(User owner) { return quoteRepository.countByOwner(owner); }
    public long countPublicByOwner(User owner) { return quoteRepository.countByOwnerAndPublicVisible(owner, true); }
    public long countActiveByOwner(User owner) { return quoteRepository.countByOwnerAndActive(owner, true); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category resolveCategory(User owner, Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findByIdAndOwner(categoryId, owner)
                .orElse(null);
    }

    private StreamSettings defaultSettings(User user) {
        return StreamSettings.builder()
                .user(user)
                .sourceMode(StreamSettings.SourceMode.COMMUNITY)
                .intervalSeconds(60)
                .build();
    }
}
