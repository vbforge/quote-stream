package com.quotestream.controller;

import com.quotestream.model.User;
import com.quotestream.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService     userService;
    private final QuoteService    quoteService;
    private final CategoryService categoryService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getByUsername(userDetails.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("totalQuotes",    quoteService.countByOwner(user));
        model.addAttribute("publicQuotes",   quoteService.countPublicByOwner(user));
        model.addAttribute("activeQuotes",   quoteService.countActiveByOwner(user));
        model.addAttribute("totalCategories", categoryService.getAllByOwner(user).size());
        model.addAttribute("recentQuotes",   quoteService.getAllByOwner(user)
                .stream().limit(5).toList());

        return "dashboard/index";
    }
}
