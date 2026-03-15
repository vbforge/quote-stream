package com.quotestream.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;
}
