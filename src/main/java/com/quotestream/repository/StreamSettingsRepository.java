package com.quotestream.repository;

import com.quotestream.model.StreamSettings;
import com.quotestream.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreamSettingsRepository extends JpaRepository<StreamSettings, Long> {
    Optional<StreamSettings> findByUser(User user);
}
