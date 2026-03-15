package com.quotestream.dto;

import com.quotestream.model.StreamSettings;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreamSettingsRequest {

    @NotNull
    private StreamSettings.SourceMode sourceMode;

    private Long categoryId;

    @NotNull
    @Min(30)
    @Max(604800)
    private Integer intervalSeconds;
}
