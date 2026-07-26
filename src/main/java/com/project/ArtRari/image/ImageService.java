package com.project.ArtRari.image;

import com.cloudinary.Cloudinary;
import com.project.ArtRari.exception.ArtrariException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final Cloudinary cloudinary;
    private final Tika tika;

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpg",
            "image/jpeg",
            "image/png",
            "image/webp");

    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Файл порожній");
        }
        try (InputStream inputStream = file.getInputStream()) {
            String detectedType = tika.detect(inputStream);
            if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
                log.warn("Спроба завантаження невалідного файлу. Визначений тип: {}", detectedType);
                throw new ArtrariException(HttpStatus.BAD_REQUEST,
                        "Дозволено завантажувати тільки зображення (jpg, jpeg, png, webp)");
            }
            Map<String, String> options = Map.of("resource_type", "image");
            Map<?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Помилка при роботі з Cloudinary або читанні файлу: {}", e.getMessage(), e);
            throw new ArtrariException(HttpStatus.INTERNAL_SERVER_ERROR, "Помилка при обробці зображення");
        }
    }
}
