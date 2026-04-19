package com.project.ArtRari.homepage;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/home")
public class HomePageController {
    private HomePageService homePageService;

    @GetMapping
    public HomePageResponse homePage() {
        return homePageService.getHomePage();
    }
}
