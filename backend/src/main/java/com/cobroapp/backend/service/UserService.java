package com.cobroapp.backend.service;

import com.cobroapp.backend.dto.user.CreateUserRequest;
import com.cobroapp.backend.dto.user.UserResponse;
import com.cobroapp.backend.entity.User;
import com.cobroapp.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public UserResponse create(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con este correo"
            );
        }

        User user = new User();

        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }
}
