package com.syllabusai.service;

import com.syllabusai.model.User;
import com.syllabusai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(String email, String password, String firstName, String lastName) {
        log.info("Creating new user: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User getOrCreateDefaultUser() {
        String defaultEmail = "default@user.com";

        return userRepository.findByEmail(defaultEmail)
                .orElseGet(() -> createUser(
                        defaultEmail,
                        "default123",
                        "Default",
                        "User"
                ));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}