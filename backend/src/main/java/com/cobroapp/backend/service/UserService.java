package com.cobroapp.backend.service;

import com.cobroapp.backend.dto.user.CreateUserRequest;
import com.cobroapp.backend.dto.user.UserResponse;
import com.cobroapp.backend.entity.User;
import com.cobroapp.backend.exception.EmailAlreadyExistsException;
import com.cobroapp.backend.repository.UserRepository;
import java.util.List;

import com.cobroapp.backend.dto.user.UpdateUserRequest;
import com.cobroapp.backend.exception.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

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

    //CREAR EL USUARIO
    public UserResponse create(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
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
   //LISTAR LOS USUARIOS
    public List<UserResponse> findAll() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "No se encontró el usuario con ID: " + id
                ));

        if (request.email() != null) {

            String email = request.email()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (email.isBlank()) {
                throw new IllegalArgumentException(
                        "El correo no puede estar vacío"
                );
            }

            if (!email.equals(user.getEmail())
                    && userRepository.existsByEmail(email)) {

                throw new EmailAlreadyExistsException(
                        "Ya existe un usuario registrado con este correo"
                );
            }

            user.setEmail(email);
        }

        if (request.status() != null) {
            user.setStatus(request.status());
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }



}
