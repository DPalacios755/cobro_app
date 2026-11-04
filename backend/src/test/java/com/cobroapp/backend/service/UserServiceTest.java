
package com.cobroapp.backend.service;

import com.cobroapp.backend.exception.EmailAlreadyExistsException;
import com.cobroapp.backend.dto.user.CreateUserRequest;
import com.cobroapp.backend.dto.user.UserResponse;
import com.cobroapp.backend.entity.User;
import com.cobroapp.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import java.util.List;
import java.time.OffsetDateTime;
import com.cobroapp.backend.dto.user.UpdateUserRequest;
import com.cobroapp.backend.exception.UserNotFoundException;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldCreateUserSuccessfully() {



        // 1. Datos que recibirá el servicio
        CreateUserRequest request = new CreateUserRequest(
                "prueba@cobroapp.com",
                "MiClave123"
        );

        // 2. Simular que el correo no existe
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        // 3. Simular el cifrado de la contraseña
        when(passwordEncoder.encode(request.password()))
                .thenReturn("password-cifrada");

        // 4. Simular que PostgreSQL guarda el usuario
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setCreatedAt(
                            java.time.OffsetDateTime.now()
                    );
                    return user;
                });

        // 5. Ejecutar el método real
        UserResponse response = userService.create(request);

        // 6. Comprobar el resultado
        assertNotNull(response);
        assertEquals("prueba@cobroapp.com", response.email());
        assertEquals("ACTIVE", response.status());

        // 7. Verificar que se cifró la contraseña
        verify(passwordEncoder).encode("MiClave123");

        // 8. Verificar que se guardó el usuario
        verify(userRepository).save(any(User.class));


    }

    @Test
    void shouldRejectDuplicateEmail() {

        // Preparar datos
        CreateUserRequest request = new CreateUserRequest(
                "existente@cobroapp.com",
                "MiClave123"
        );

        // Simular que el correo ya existe
        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // Verificar que se lance la excepción
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.create(request)
        );

        assertEquals(
                "Ya existe un usuario registrado con este correo",
                exception.getMessage()
        );

        // Verificar que no se guarde ningún usuario
        verify(userRepository, never()).save(any(User.class));

        // Verificar que tampoco se cifre la contraseña
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldSaveEncodedPassword() {

        CreateUserRequest request = new CreateUserRequest(
                "seguridad@cobroapp.com",
                "MiClave123"
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode("MiClave123"))
                .thenReturn("HASH_SIMULADO");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar el servicio
        userService.create(request);

        // Capturar el usuario enviado al repositorio
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        // Comprobar el valor almacenado
        assertEquals("HASH_SIMULADO", savedUser.getPassword());

        assertNotEquals("MiClave123", savedUser.getPassword());
    }
    @Test
    void shouldListUsersSuccessfully() {

        // 1. Crear usuarios simulados
        User user1 = new User();
        user1.setEmail("usuario1@cobroapp.com");
        user1.setPassword("HASH_1");
        user1.setStatus("ACTIVE");
        user1.setCreatedAt(OffsetDateTime.now());

        User user2 = new User();
        user2.setEmail("usuario2@cobroapp.com");
        user2.setPassword("HASH_2");
        user2.setStatus("INACTIVE");
        user2.setCreatedAt(OffsetDateTime.now());

        // 2. Simular respuesta del repositorio
        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        // 3. Ejecutar método real del servicio
        List<UserResponse> response = userService.findAll();

        // 4. Verificar resultados
        assertEquals(2, response.size());

        assertEquals(
                "usuario1@cobroapp.com",
                response.get(0).email()
        );

        assertEquals("ACTIVE", response.get(0).status());

        assertEquals(
                "usuario2@cobroapp.com",
                response.get(1).email()
        );

        assertEquals("INACTIVE", response.get(1).status());

        // 5. Verificar interacción con el repositorio
        verify(userRepository).findAll();
    }
    @Test
    void shouldUpdateUserSuccessfully() {

        // 1. Simular un usuario existente
        User user = new User();
        user.setEmail("anterior@cobroapp.com");
        user.setPassword("HASH_EXISTENTE");
        user.setStatus("ACTIVE");
        user.setCreatedAt(
                java.time.OffsetDateTime.now()
        );

        // 2. Solicitar únicamente un cambio de correo
        UpdateUserRequest request = new UpdateUserRequest(
                "nuevo@cobroapp.com",
                null
        );

        // 3. Simular la búsqueda del usuario
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // 4. Simular que el nuevo correo está disponible
        when(userRepository.existsByEmail("nuevo@cobroapp.com"))
                .thenReturn(false);

        // 5. Ejecutar el servicio
        UserResponse response = userService.update(1L, request);

        // 6. Verificar que el correo cambió
        assertEquals("nuevo@cobroapp.com", response.email());

        // 7. Verificar que el estado se conservó
        assertEquals("ACTIVE", response.status());

        // 8. Verificar que la contraseña no cambió
        assertEquals("HASH_EXISTENTE", user.getPassword());

        // 9. Verificar que se consultó el repositorio
        verify(userRepository).findById(1L);
    }
    @Test
    void shouldRejectUpdateWhenUserDoesNotExist() {

        // Preparar la solicitud
        UpdateUserRequest request = new UpdateUserRequest(
                "nuevo@cobroapp.com",
                "ACTIVE"
        );

        // Simular que el usuario no existe
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Comprobar que se lanza la excepción
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.update(999L, request)
        );

        assertEquals(
                "No se encontró el usuario con ID: 999",
                exception.getMessage()
        );

        verify(userRepository).findById(999L);

        // No debe intentar guardar un usuario nuevo
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void shouldRejectUpdateWithDuplicateEmail() {

        // Usuario existente
        User user = new User();
        user.setEmail("original@cobroapp.com");
        user.setPassword("HASH_EXISTENTE");
        user.setStatus("ACTIVE");

        // Intento de cambiar a un correo ocupado
        UpdateUserRequest request = new UpdateUserRequest(
                "ocupado@cobroapp.com",
                null
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("ocupado@cobroapp.com"))
                .thenReturn(true);

        // Comprobar que rechaza la actualización
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.update(1L, request)
        );

        // El correo original debe permanecer intacto
        assertEquals(
                "original@cobroapp.com",
                user.getEmail()
        );

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("ocupado@cobroapp.com");
    }
}
