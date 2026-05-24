package com.project.ArtRari.user;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.dto.PasswordChangeRequest;
import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.ProfileUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + username)
        );
        return UserDetailsImpl.build(user);
    }

    public ProfileResponse getProfile(UserDetailsImpl udi) {
        User user = userRepository.findById(udi.getId()).orElseThrow(
                () -> new ArtrariException(HttpStatus.NOT_FOUND, "Такого користувача не існує")
        );
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(UserDetailsImpl udi, ProfileUpdateRequest profileUpdateRequest) {
        if (userRepository.existsByEmail(profileUpdateRequest.newEmail())) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Цей email вже зайнятий");
        }
        if (userRepository.existsByPhone(profileUpdateRequest.newPhone())) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Цей телефон вже зайнятий");
        }
        User user = userRepository.findById(udi.getId()).orElseThrow(
                () -> new ArtrariException(HttpStatus.NOT_FOUND, "Такого користувача не існує")
        );
        user.setFirstName(profileUpdateRequest.newFirstName());
        user.setLastName(profileUpdateRequest.newLastName());
        user.setPhone(profileUpdateRequest.newPhone());
        user.setEmail(profileUpdateRequest.newEmail());
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void changePassword(UserDetailsImpl udi, PasswordChangeRequest passwordChangeRequest) {
        User user = userRepository.findById(udi.getId()).orElseThrow(
                () -> new ArtrariException(HttpStatus.NOT_FOUND, "Такого користувача не існує")
        );
        if (!passwordEncoder.matches(passwordChangeRequest.oldPassword(), user.getPassword())) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Невірний старий пароль");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.newPassword()));
    }

}
