package com.cobroapp.backend.controller;

import com.cobroapp.backend.dto.user.CreateUserRequest;
import com.cobroapp.backend.dto.user.UserResponse;
import com.cobroapp.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.cobroapp.backend.dto.user.UpdateUserRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Crear usuario
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {

        UserResponse user = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    // Listar usuarios
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {

        List<UserResponse> users = userService.findAll();

        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        UserResponse user = userService.update(id, request);

        return ResponseEntity.ok(user);
    }

}