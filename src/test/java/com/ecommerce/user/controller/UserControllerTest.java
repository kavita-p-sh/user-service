package com.ecommerce.user.controller;

import com.ecommerce.common.util.AppConstants;
import com.ecommerce.user.dto.UpdateUserDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController.
 *
 * This test class uses Mockito only.
 * No Spring context is loaded and @Autowired is not used.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    /**
     * Tests fetching all users with filters.
     */
    @Test
    void getUsers_success() {
        UserResponseDTO user = new UserResponseDTO();
        user.setUserId(UUID.randomUUID());
        user.setUsername("RamPatel");
        user.setEmail("ram@gmail.com");
        user.setPhoneNumber("9876543210");

        when(userService.getUsers("RamPatel", "ram@gmail.com", "9876543210"))
                .thenReturn(List.of(user));

        ResponseEntity<List<UserResponseDTO>> result =
                userController.getUsers("RamPatel", "ram@gmail.com", "9876543210");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("RamPatel", result.getBody().get(0).getUsername());
        assertEquals("ram@gmail.com", result.getBody().get(0).getEmail());

        verify(userService).getUsers("RamPatel", "ram@gmail.com", "9876543210");
    }

    /**
     * Tests fetching current logged-in user profile.
     */
    @Test
    void getMyProfile_success() {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUsername("RamPatel");
        response.setEmail("ram@gmail.com");

        when(userService.getMyProfile()).thenReturn(response);

        ResponseEntity<UserResponseDTO> result = userController.getMyProfile();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("RamPatel", result.getBody().getUsername());
        assertEquals("ram@gmail.com", result.getBody().getEmail());

        verify(userService).getMyProfile();
    }

    /**
     * Tests admin updating user by username.
     */
    @Test
    void updateUser_success() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setUsername("RamPatel");
        request.setEmail("newram@gmail.com");
        request.setPhoneNumber("9876543210");

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUsername("RamPatel");
        response.setEmail("newram@gmail.com");
        response.setPhoneNumber("9876543210");

        when(userService.updateUserByUsername("RamPatel", request))
                .thenReturn(response);

        ResponseEntity<UserResponseDTO> result = userController.updateUser(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("RamPatel", result.getBody().getUsername());
        assertEquals("newram@gmail.com", result.getBody().getEmail());

        verify(userService).updateUserByUsername("RamPatel", request);
    }

    /**
     * Tests current user profile update.
     */
    @Test
    void updateMyProfile_success() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setUsername("RamPatel");
        request.setEmail("updated@gmail.com");
        request.setPhoneNumber("9876543210");

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUsername("RamPatel");
        response.setEmail("updated@gmail.com");
        response.setPhoneNumber("9876543210");

        when(userService.updateMyProfile(request)).thenReturn(response);

        ResponseEntity<UserResponseDTO> result = userController.updateMyProfile(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("RamPatel", result.getBody().getUsername());
        assertEquals("updated@gmail.com", result.getBody().getEmail());

        verify(userService).updateMyProfile(request);
    }

    /**
     * Tests deleting user by username.
     */
    @Test
    void deleteUser_success() {
        String username = "RamPatel";

        doNothing().when(userService).deleteUserByUsername(username);

        ResponseEntity<String> result = userController.deleteUser(username);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(String.format(AppConstants.USER_DELETED_SUCCESS, username), result.getBody());

        verify(userService).deleteUserByUsername(username);
    }
    /**
     * Tests fetching user by username.
     */
    @Test
    void getUserByUsername_success() {
        String username = "RamPatel";

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(UUID.randomUUID());
        response.setUsername(username);
        response.setEmail("ram@gmail.com");

        when(userService.getUserByUsername(username)).thenReturn(response);

        ResponseEntity<UserResponseDTO> result =
                userController.getUserByUsername(username);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(username, result.getBody().getUsername());
        assertEquals("ram@gmail.com", result.getBody().getEmail());

        verify(userService).getUserByUsername(username);
    }
}