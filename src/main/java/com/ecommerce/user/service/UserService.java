package com.ecommerce.user.service;
import com.ecommerce.user.dto.RegisterRequestDTO;
import com.ecommerce.user.dto.UpdateUserDTO;
import com.ecommerce.user.dto.UserResponseDTO;

import java.util.List;

/**
 * Service interface for managing user operations.
 * Provides methods for user registration, fetching,
 * updating, and deleting users.
 */
public interface UserService {


    /**
     * Registers a new user.
     *
     * @param dto user request data
     * @return registered user details
     */
    UserResponseDTO registerUser(RegisterRequestDTO dto);

    /**
     * Fetches users based on username, email, or phone.
     */
    List<UserResponseDTO> getUsers(String username, String email, String phoneNumber);
    /**
     * Fetches user by email.
     */
    UserResponseDTO getUserByEmail(String email);
    /**
     * Fetches user by username.
     */
    UserResponseDTO getUserByUsername(String username);

    /**
     * Fetches user by phone.
     */
    UserResponseDTO getUserByPhoneNumber(String phoneNumber);

    /**
     * Fetches allUsers.
     */
    List<UserResponseDTO> getAllUsers();

    /**
     *
     * @return logged-in user's profile.
     */
    UserResponseDTO getMyProfile();

    /**
     * By Admin
     * @param username update user by username
     * @param dto updated user data
     * @return updated user
     */
    UserResponseDTO updateUserByUsername(String username, UpdateUserDTO dto);

    /**
     * Updates logged-in user's profile.
     */
    UserResponseDTO updateMyProfile(UpdateUserDTO dto);

    /**
     *
     * @param username delete user by username
     */
    void deleteUserByUsername(String username);


}