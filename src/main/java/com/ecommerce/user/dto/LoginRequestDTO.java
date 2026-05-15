package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for user login.
 */

@Data
public class LoginRequestDTO {

    @NotBlank(message = "{username.required}")
    private String username;

    @NotBlank(message = "{user.password.required}")
    private String password;
}
