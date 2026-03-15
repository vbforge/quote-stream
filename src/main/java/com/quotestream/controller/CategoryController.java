package com.quotestream.controller;

import com.quotestream.dto.CategoryRequest;
import com.quotestream.model.User;
import com.quotestream.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService     userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getByUsername(userDetails.getUsername());
        model.addAttribute("categories",     categoryService.getAllByOwnerWithCounts(user));
        model.addAttribute("categoryRequest", new CategoryRequest());
        return "categories/list";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        User user = userService.getByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllByOwnerWithCounts(user));
            return "categories/list";
        }

        try {
            categoryService.create(user, request.getName());
            redirectAttributes.addFlashAttribute("success", "Category created!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/rename")
    public String rename(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @RequestParam String name,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        try {
            categoryService.rename(user, id, name);
            redirectAttributes.addFlashAttribute("success", "Category renamed!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(userDetails.getUsername());
        try {
            categoryService.delete(user, id);
            redirectAttributes.addFlashAttribute("success", "Category deleted. Quotes in this category are now uncategorized.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }
}
