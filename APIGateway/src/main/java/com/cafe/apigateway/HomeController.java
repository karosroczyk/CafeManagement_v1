package com.cafe.apigateway;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "API Gateway Home");
        model.addAttribute("description", "Welcome to the API Gateway. Select an endpoint below to get started.");
        return "home";
    }
}
