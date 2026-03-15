package com.quotestream.dto;

import com.quotestream.model.Quote;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteResponse {

    private Long id;
    private String text;
    private String author;
    private String category;
    private int nextInSeconds;

    public static QuoteResponse from(Quote quote, int nextInSeconds) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .text(quote.getText())
                .author(quote.getAuthor())
                .category(quote.getCategory() != null ? quote.getCategory().getName() : null)
                .nextInSeconds(nextInSeconds)
                .build();
    }
}
