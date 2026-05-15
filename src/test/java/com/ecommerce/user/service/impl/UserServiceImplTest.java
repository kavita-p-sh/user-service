package com.ecommerce.user.service.impl;


import com.ecommerce.common.entity.RoleEntity;
import com.ecommerce.common.entity.UserEntity;
import com.ecommerce.common.enums.RoleName;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.repository.RoleRepository;
import com.ecommerce.common.repository.UserRepository;
import com.ecommerce.user.dto.RegisterRequestDTO;
import com.ecommerce.user.dto.UpdateUserDTO;
import com.ecommerce.user.dto.UserResponseDTO;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.service.LoggedInUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



/**
 * Unit test class for {@link UserServiceImpl}.
 *
 * <p>This class tests user registration, fetching users, updating user details,
 * logged-in user profile operations, and deleting users using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoggedInUserService loggedInUserService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity user;
    private RoleEntity userRole;
    private UserResponseDTO userResponseDTO;


    /**
     * Initializes common test data before each test case.
     */
    @BeforeEach
    void setUp() {
        userRole = new RoleEntity();
        userRole.setRoleId(1L);
        userRole.setRoleName(RoleName.USER);

        user = new UserEntity();
        user.setUserId(UUID.randomUUID());
        user.setUsername("kavita");
        user.setEmail("kavita@gmail.com");
        user.setPhoneNumber("9876543210");
        user.setPassword("encodedPassword");
        user.setRole(userRole);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername("kavita");
        userResponseDTO.setEmail("kavita@gmail.com");
        userResponseDTO.setPhoneNumber("9876543210");
        userResponseDTO.setRole(RoleName.USER);
    }

    @Test
    void registerUser_ShouldRegisterUser() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("kavita");
        request.setEmail("kavita@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("password");
        request.setRole(RoleName.USER);

        when(userRepository.existsByUsername("kavita")).thenReturn(false);
        when(userRepository.existsByEmail("kavita@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("kavita", response.getUsername());
        assertEquals("kavita@gmail.com", response.getEmail());

        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toDTO(user);
    }

    @Test
    void registerUser_ShouldThrowBadRequestException() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("kavita");
        request.setEmail("kavita@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("password");
        request.setRole(RoleName.USER);

        when(userRepository.existsByUsername("kavita")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowBadRequestException_WhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("kavita");
        request.setEmail("kavita@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("password");
        request.setRole(RoleName.USER);

        when(userRepository.existsByUsername("kavita")).thenReturn(false);
        when(userRepository.existsByEmail("kavita@gmail.com")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowBadRequestException_WhenPhoneNumberAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("kavita");
        request.setEmail("kavita@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("password");
        request.setRole(RoleName.USER);

        when(userRepository.existsByUsername("kavita")).thenReturn(false);
        when(userRepository.existsByEmail("kavita@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("kavita");
        request.setEmail("kavita@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("password");
        request.setRole(RoleName.USER);

        when(userRepository.existsByUsername("kavita")).thenReturn(false);
        when(userRepository.existsByEmail("kavita@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.getUserByUsername("kavita");

        assertNotNull(response);
        assertEquals("kavita", response.getUsername());

        verify(userRepository).findByUsername("kavita");
        verify(userMapper).toDTO(user);
    }

    @Test
    void getUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByUsername("wrong")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername("wrong"));

        verify(userMapper, never()).toDTO(any(UserEntity.class));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("kavita@gmail.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.getUserByEmail("kavita@gmail.com");

        assertNotNull(response);
        assertEquals("kavita@gmail.com", response.getEmail());

        verify(userRepository).findByEmail("kavita@gmail.com");
    }

    @Test
    void getUserByPhoneNumber_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.getUserByPhoneNumber("9876543210");

        assertNotNull(response);
        assertEquals("9876543210", response.getPhoneNumber());

        verify(userRepository).findByPhoneNumber("9876543210");
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> response = userService.getAllUsers();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("kavita", response.get(0).getUsername());

        verify(userRepository).findAll();
    }

    @Test
    void getUsers_ShouldReturnUserByUsername_WhenUsernameIsPresent() {
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> response = userService.getUsers("kavita", null, null);

        assertEquals(1, response.size());
        assertEquals("kavita", response.get(0).getUsername());

        verify(userRepository).findByUsername("kavita");
        verify(userRepository, never()).findAll();
    }

    @Test
    void getUsers_ShouldReturnUserByEmail_WhenEmailIsPresent() {
        when(userRepository.findByEmail("kavita@gmail.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> response = userService.getUsers(null, "kavita@gmail.com", null);

        assertEquals(1, response.size());
        assertEquals("kavita@gmail.com", response.get(0).getEmail());

        verify(userRepository).findByEmail("kavita@gmail.com");
        verify(userRepository, never()).findAll();
    }

    @Test
    void getUsers_ShouldReturnAllUsers_WhenNoFilterIsPresent() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        List<UserResponseDTO> response = userService.getUsers(null, null, null);

        assertEquals(1, response.size());

        verify(userRepository).findAll();
    }

    @Test
    void getMyProfile_ShouldReturnLoggedInUserProfile() {
        when(loggedInUserService.getUsername()).thenReturn("kavita");
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.getMyProfile();

        assertNotNull(response);
        assertEquals("kavita", response.getUsername());

        verify(loggedInUserService).getUsername();
        verify(userRepository).findByUsername("kavita");
    }

    @Test
    void getMyProfile_ShouldThrowResourceNotFoundException_WhenLoggedInUserNotFound() {
        when(loggedInUserService.getUsername()).thenReturn("kavita");
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getMyProfile());

        verify(userMapper, never()).toDTO(any(UserEntity.class));
    }

    @Test
    void updateUserByUsername_ShouldUpdateUser_WhenValidRequest() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setEmail("newemail@gmail.com");
        request.setPhoneNumber("9999999999");
        request.setPassword("newPassword");

        UserEntity updatedUser = new UserEntity();
        updatedUser.setUserId(UUID.randomUUID());
        updatedUser.setUsername("kavita");
        updatedUser.setEmail("newemail@gmail.com");
        updatedUser.setPhoneNumber("9999999999");
        updatedUser.setPassword("newEncodedPassword");
        updatedUser.setRole(userRole);

        UserResponseDTO updatedResponse = new UserResponseDTO();
        updatedResponse.setUsername("kavita");
        updatedResponse.setEmail("newemail@gmail.com");
        updatedResponse.setPhoneNumber("9999999999");
        updatedResponse.setRole(RoleName.USER);

        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9999999999")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedResponse);

        UserResponseDTO response = userService.updateUserByUsername("kavita", request);

        assertNotNull(response);
        assertEquals("newemail@gmail.com", response.getEmail());
        assertEquals("9999999999", response.getPhoneNumber());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setEmail("newemail@gmail.com");

        when(userRepository.findByUsername("wrong")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserByUsername("wrong", request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUserByUsername_ShouldThrowBadRequestException_WhenEmailAlreadyExists() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setEmail("already@gmail.com");

        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("already@gmail.com")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.updateUserByUsername("kavita", request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUserByUsername_ShouldThrowBadRequestException_WhenPhoneNumberAlreadyExists() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setPhoneNumber("9999999999");

        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber("9999999999")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.updateUserByUsername("kavita", request));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateMyProfile_ShouldUpdateLoggedInUserProfile() {
        UpdateUserDTO request = new UpdateUserDTO();
        request.setEmail("newemail@gmail.com");

        UserEntity updatedUser = new UserEntity();
        updatedUser.setUserId(UUID.randomUUID());
        updatedUser.setUsername("kavita");
        updatedUser.setEmail("newemail@gmail.com");
        updatedUser.setPhoneNumber("9876543210");
        updatedUser.setRole(userRole);

        UserResponseDTO updatedResponse = new UserResponseDTO();
        updatedResponse.setUsername("kavita");
        updatedResponse.setEmail("newemail@gmail.com");
        updatedResponse.setPhoneNumber("9876543210");
        updatedResponse.setRole(RoleName.USER);

        when(loggedInUserService.getUsername()).thenReturn("kavita");
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@gmail.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedResponse);

        UserResponseDTO response = userService.updateMyProfile(request);

        assertNotNull(response);
        assertEquals("newemail@gmail.com", response.getEmail());

        verify(loggedInUserService).getUsername();
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserByUsername_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.findByUsername("kavita")).thenReturn(Optional.of(user));

        userService.deleteUserByUsername("kavita");

        verify(userRepository).findByUsername("kavita");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByUsername("wrong")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUserByUsername("wrong"));

        verify(userRepository, never()).delete(any(UserEntity.class));
    }
}