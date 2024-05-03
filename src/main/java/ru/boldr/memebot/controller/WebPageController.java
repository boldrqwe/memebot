package ru.boldr.memebot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String home(Model model) {
        return "index";  // имя HTML файла в папке resources/templates
    }
}
