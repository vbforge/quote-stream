package com.quotestream.service;

import com.quotestream.dto.StreamSettingsRequest;
import com.quotestream.model.Category;
import com.quotestream.model.StreamSettings;
import com.quotestream.model.User;
import com.quotestream.repository.CategoryRepository;
import com.quotestream.repository.StreamSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamSettingsService {

    private final StreamSettingsRepository streamSettingsRepository;
    private final CategoryRepository categoryRepository;

    public static final List<Integer> INTERVAL_OPTIONS = List.of(
            30, 60, 120, 300, 600, 1800, 3600, 21600, 86400, 604800
    );

    public StreamSettings getOrCreate(User user) {
        return streamSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    StreamSettings s = StreamSettings.builder()
                            .user(user)
                            .sourceMode(StreamSettings.SourceMode.COMMUNITY)
                            .intervalSeconds(60)
                            .build();
                    return streamSettingsRepository.save(s);
                });
    }

    @Transactional
    public StreamSettings update(User user, StreamSettingsRequest request) {
        StreamSettings settings = getOrCreate(user);

        settings.setSourceMode(request.getSourceMode());
        settings.setIntervalSeconds(request.getIntervalSeconds());

        if (request.getCategoryId() != null) {
            categoryRepository.findByIdAndOwner(request.getCategoryId(), user)
                    .ifPresent(settings::setCategory);
        } else {
            settings.setCategory(null);
        }

        return streamSettingsRepository.save(settings);
    }

    public static String formatInterval(int seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + " min";
        if (seconds < 86400) return (seconds / 3600) + " hr";
        if (seconds < 604800) return (seconds / 86400) + " day" + (seconds / 86400 > 1 ? "s" : "");
        return "1 week";
    }
}
