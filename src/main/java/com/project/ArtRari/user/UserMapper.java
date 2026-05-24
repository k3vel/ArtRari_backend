package com.project.ArtRari.user;

import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserPreviewResponse toUserPreviewResponse(User user) {
        return new UserPreviewResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail()
        );
    }
}
