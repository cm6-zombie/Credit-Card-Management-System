
package com.creditcard.backend.service;

import com.creditcard.backend.dto.SignupRequest;
import com.creditcard.backend.dto.SignupResponse;
import com.creditcard.backend.entity.User;
import com.creditcard.backend.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse register(SignupRequest request) {

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        // BCrypt accepts at most 72 bytes of input.
        if (request.password().getBytes(
                StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password exceeds 72 UTF-8 bytes"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.name().trim(),
                email,
                hashedPassword
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new SignupResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "User registered successfully"
            );

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration conflicts with existing data",
                    ex
            );
        }
    }
}
