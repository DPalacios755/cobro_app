package com.cobroapp.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(

        @Email(message = "El correo no tiene un formato válido")
        String email,

        @Pattern(
                regexp = "ACTIVE|INACTIVE",
                message = "El estado debe ser ACTIVE o INACTIVE"
        )
        String status

) {
}