package com.cafe.apigateway.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Cafe");
        model.addAttribute("description", "Welcome to our cafe. Browse our menu or place an order.");
        return "home";
    }
}
