package com.quotestream;

import com.quotestream.repository.QuoteRepository;
import com.quotestream.repository.UserRepository;
import com.quotestream.service.UserService;
import com.quotestream.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuoteStreamApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        // Verifies Spring context starts and DB migrations ran successfully
        assertThat(userRepository).isNotNull();
        assertThat(quoteRepository).isNotNull();
    }

    @Test
    void databaseHasSeedData() {
        // Flyway migration includes seed data — demo user should exist
        assertThat(userRepository.existsByUsername("demo")).isTrue();
    }

    @Test
    void registerNewUser_succeeds() {
        RegisterRequest req = new RegisterRequest(
                "testuser", "test@example.com", "password123", "password123"
        );
        var user = userService.register(req);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
    }

    @Test
    void registerDuplicateUsername_throws() {
        RegisterRequest req = new RegisterRequest(
                "demo", "other@example.com", "password123", "password123"
        );
        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void seedQuotesArePublicAndActive() {
        var quotes = quoteRepository.findRandomPublicActive();
        assertThat(quotes).isNotEmpty();
        quotes.forEach(q -> {
            assertThat(q.isPublicVisible()).isTrue();
            assertThat(q.isActive()).isTrue();
        });
    }
}
