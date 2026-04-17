package com.project.ArtRari.homepage;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController("/home")
public class HomePageController {
    private HomePageService homePageService;

    @GetMapping("")
    public HomePage homePage() {
        return homePageService.getHomePage();
    }
}
