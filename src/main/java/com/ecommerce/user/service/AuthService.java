package com.ecommerce.user.service;

import com.ecommerce.user.dto.LoginRequestDTO;
import com.ecommerce.user.dto.RegisterRequestDTO;
import com.ecommerce.user.dto.UserResponseDTO;

public interface AuthService {
    /**
     * Authenticates user and returns JWT token.
     *
     * @param dto login request data
     * @return JWT token
     */
    String login(LoginRequestDTO dto);

    /**
     * Registers a new user.
     *
     * @param dto register request data
     * @return user response
     */
    UserResponseDTO register(RegisterRequestDTO dto);
}