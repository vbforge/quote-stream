package com.quotestream.controller;

import com.quotestream.dto.QuoteResponse;
import com.quotestream.dto.StreamSettingsRequest;
import com.quotestream.model.*;
import com.quotestream.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class StreamController {

    private static final String SESSION_INTERVAL  = "anon_interval";
    private static final int    DEFAULT_INTERVAL  = 60;

    private final QuoteService         quoteService;
    private final StreamSettingsService streamSettingsService;
    private final UserService          userService;
    private final CategoryService      categoryService;

    // ── Main page ─────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String streamPage(@AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session,
                             Model model) {

        if (userDetails != null) {
            User user = userService.getByUsername(userDetails.getUsername());
            StreamSettings settings = streamSettingsService.getOrCreate(user);

            Optional<Quote> quote = quoteService.getNextForUser(user);
            model.addAttribute("quote", quote.orElse(null));
            model.addAttribute("settings", settings);
            model.addAttribute("intervalSeconds", settings.getIntervalSeconds());
            model.addAttribute("intervalOptions", StreamSettingsService.INTERVAL_OPTIONS);
            model.addAttribute("sourceModes", StreamSettings.SourceMode.values());
            model.addAttribute("categories", categoryService.getAllByOwner(user));
        } else {
            int interval = getAnonInterval(session);
            Optional<Quote> quote = quoteService.getNextForAnonymous();
            model.addAttribute("quote", quote.orElse(null));
            model.addAttribute("intervalSeconds", interval);
            model.addAttribute("intervalOptions", StreamSettingsService.INTERVAL_OPTIONS);
        }

        return "stream";
    }

    // ── REST: next quote ──────────────────────────────────────────────────────

    @GetMapping("/api/quote/next")
    @ResponseBody
    public ResponseEntity<QuoteResponse> nextQuote(@AuthenticationPrincipal UserDetails userDetails,
                                                    HttpSession session) {
        if (userDetails != null) {
            User user = userService.getByUsername(userDetails.getUsername());
            StreamSettings settings = streamSettingsService.getOrCreate(user);
            return quoteService.getNextForUser(user)
                    .map(q -> ResponseEntity.ok(QuoteResponse.from(q, settings.getIntervalSeconds())))
                    .orElse(ResponseEntity.noContent().build());
        } else {
            int interval = getAnonInterval(session);
            return quoteService.getNextForAnonymous()
                    .map(q -> ResponseEntity.ok(QuoteResponse.from(q, interval)))
                    .orElse(ResponseEntity.noContent().build());
        }
    }

    // ── REST: update stream settings ──────────────────────────────────────────

    @PostMapping("/api/stream/settings")
    @ResponseBody
    public ResponseEntity<?> updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody StreamSettingsRequest request,
                                             HttpSession session) {
        if (userDetails != null) {
            User user = userService.getByUsername(userDetails.getUsername());
            streamSettingsService.update(user, request);
        } else {
            // Anonymous: persist only interval in session
            if (request.getIntervalSeconds() != null) {
                session.setAttribute(SESSION_INTERVAL, request.getIntervalSeconds());
            }
        }
        return ResponseEntity.ok().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int getAnonInterval(HttpSession session) {
        Object val = session.getAttribute(SESSION_INTERVAL);
        return (val instanceof Integer i) ? i : DEFAULT_INTERVAL;
    }
}
