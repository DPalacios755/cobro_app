package com.cobroapp.backend.service;

import com.cobroapp.backend.dto.auth.LoginRequest;
import com.cobroapp.backend.dto.auth.LoginResponse;
import com.cobroapp.backend.entity.User;
import com.cobroapp.backend.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        // 1. Buscar usuario por correo
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Correo o contraseña incorrectos"
                        )
                );

        // 2. Verificar contraseña con BCrypt
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos"
            );
        }

        // 3. Verificar estado del usuario
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new DisabledException(
                    "La cuenta está deshabilitada"
            );
        }



        // 4. Devolver resultado
        String token = jwtService.generateToken(user.getId());
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                token,
                "Bearer",
                "Credenciales verificadas correctamente"
        );
    }
}