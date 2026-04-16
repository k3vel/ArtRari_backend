package com.project.ArtRari;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String index() {
        return "Hello World";
    }

    @GetMapping("/secured/abc")
    public String secured() {
        return "This is a secured page";
    }

    @GetMapping("/secured/admin/admin-feature")
    public String adminFeature() {
        return "This is a admin page";
    }
}
