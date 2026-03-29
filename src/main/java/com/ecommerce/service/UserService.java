package com.ecommerce.service;

import com.ecommerce.dto.UserResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UserResponse promoteToAdmin(Long id) {
        User user = findOrThrow(id);
        user.getRoles().add("ROLE_ADMIN");
        userRepository.save(user);
        log.info("User '{}' promoted to ADMIN", user.getUsername());
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findOrThrow(id);
        user.setEnabled(false);           // soft-disable instead of hard delete
        userRepository.save(user);
        log.info("User '{}' disabled", user.getUsername());
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phoneNumber(u.getPhoneNumber())
                .roles(u.getRoles())
                .provider(u.getProvider())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
