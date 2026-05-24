package com.project.ArtRari.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/artworks/image")
    public ResponseEntity<?> uploadArtworkImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.created(URI.create(imageUrl)).body(Map.of("imageUrl", imageUrl));
    }

    @PostMapping("/exhibitions/background")
    public ResponseEntity<?> uploadExhibitionBackground(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.created(URI.create(imageUrl)).body(Map.of("imageUrl", imageUrl));
    }

    @PostMapping("/exhibitions/thumbnail")
    public ResponseEntity<?> uploadExhibitionThumbnail(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.created(URI.create(imageUrl)).body(Map.of("imageUrl", imageUrl));
    }
}
