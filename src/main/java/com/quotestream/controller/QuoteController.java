package com.quotestream.controller;

import com.quotestream.dto.QuoteRequest;
import com.quotestream.model.User;
import com.quotestream.service.CategoryService;
import com.quotestream.service.QuoteService;
import com.quotestream.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService    quoteService;
    private final CategoryService categoryService;
    private final UserService     userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getByUsername(userDetails.getUsername());
        model.addAttribute("quotes",     quoteService.getAllByOwner(user));
        model.addAttribute("categories", categoryService.getAllByOwner(user));
        model.addAttribute("quoteRequest", new QuoteRequest());
        return "quotes/list";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("quoteRequest") QuoteRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        User user = userService.getByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("quotes",     quoteService.getAllByOwner(user));
            model.addAttribute("categories", categoryService.getAllByOwner(user));
            return "quotes/list";
        }

        quoteService.create(user, request);
        redirectAttributes.addFlashAttribute("success", "Quote added!");
        return "redirect:/quotes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id,
                           Model model) {
        User user = userService.getByUsername(userDetails.getUsername());
        model.addAttribute("quote",      quoteService.getByIdAndOwner(id, user));
        model.addAttribute("categories", categoryService.getAllByOwner(user));
        model.addAttribute("quoteRequest", new QuoteRequest());
        return "quotes/edit";
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("quoteRequest") QuoteRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        User user = userService.getByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("quote",      quoteService.getByIdAndOwner(id, user));
            model.addAttribute("categories", categoryService.getAllByOwner(user));
            return "quotes/edit";
        }

        quoteService.update(user, id, request);
        redirectAttributes.addFlashAttribute("success", "Quote updated!");
        return "redirect:/quotes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        quoteService.delete(user, id);
        redirectAttributes.addFlashAttribute("success", "Quote deleted.");
        return "redirect:/quotes";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        quoteService.toggleActive(user, id);
        return "redirect:/quotes";
    }

    @PostMapping("/{id}/toggle-public")
    public String togglePublic(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        quoteService.togglePublic(user, id);
        return "redirect:/quotes";
    }
}
