package com.project.ArtRari.image;

import com.cloudinary.Cloudinary;
import com.project.ArtRari.exception.ArtrariException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private boolean isValidExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    public String uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new ArtrariException(HttpStatus.BAD_REQUEST, "Файл порожній");
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || isValidExtension(originalFilename)) {
                throw new ArtrariException(HttpStatus.BAD_REQUEST,
                        "Дозволено завантажувати тільки зображення (jpg, jpeg, png, webp)");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ArtrariException(HttpStatus.BAD_REQUEST, "Дозволено завантажувати тільки зображення");
            }
            Map<String, String> options = Map.of("resource_type", "image");
            Map<?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ArtrariException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
} //todo проверка файлов через apache tika
