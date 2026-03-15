package com.quotestream.dto;

import com.quotestream.model.Category;
import lombok.*;

@Getter
@AllArgsConstructor
public class CategoryView {
    private final Long   id;
    private final String name;
    private final long   quoteCount;

    public static CategoryView of(Category cat, long count) {
        return new CategoryView(cat.getId(), cat.getName(), count);
    }
}
