package com.quotestream.service;

import com.quotestream.dto.RegisterRequest;
import com.quotestream.model.StreamSettings;
import com.quotestream.model.User;
import com.quotestream.repository.StreamSettingsRepository;
import com.quotestream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StreamSettingsRepository streamSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        // Create default stream settings for new user
        StreamSettings settings = StreamSettings.builder()
                .user(user)
                .sourceMode(StreamSettings.SourceMode.COMMUNITY)
                .intervalSeconds(60)
                .build();
        streamSettingsRepository.save(settings);

        return user;
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
