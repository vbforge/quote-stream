package com.quotestream.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {

    @NotBlank(message = "Quote text cannot be empty")
    @Size(min = 3, max = 2000, message = "Quote must be between 3 and 2000 characters")
    private String text;

    @Size(max = 255, message = "Author name too long")
    private String author;

    private boolean publicVisible = true;

    private Long categoryId;
}
