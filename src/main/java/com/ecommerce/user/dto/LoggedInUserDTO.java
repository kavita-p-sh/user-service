package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Data Transfer Object used to store logged-in user details.
 *
 * This DTO contains basic information about the currently authenticated user,
 * such as username and role. It can be used while reading user details from
 * the security context or while passing logged-in user information inside
 * the application.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedInUserDTO {
    private String username;
    private String role;
}
