package com.ecommerce.user.service.impl;

import com.ecommerce.common.enums.RoleName;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.common.util.AppConstants;
import com.ecommerce.common.util.CacheConstant;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.RoleEntity;
import com.ecommerce.user.entity.UserEntity;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.service.LoggedInUserService;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoggedInUserService loggedInUserService;
    private final UserMapper userMapper;


    /**
     * Registers a new user.
     *
     * @param dto user request data
     * @return saved user entity
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstant.USERS, key = CacheConstant.ALL_USERS_KEY)
    public UserResponseDTO registerUser(RegisterRequestDTO dto) {
        log.info("Register request received for username: {}", dto.getUsername());

        RoleName roleName = resolveAndValidateRole(dto.getRole());
        UserEntity savedUser = createUser(dto, roleName);

        return userMapper.toDTO(savedUser);
    }

    /**
     * Creates and saves a new user.
     *
     * @param dto      user data
     * @param roleName user role
     * @return saved user
     */

    private UserEntity createUser(RegisterRequestDTO dto, RoleName roleName) {
        validateUserNotExists(dto);

        RoleEntity role = getValidRole(roleName);
        UserEntity user = createNewUser(dto, role);

        UserEntity savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return savedUser;
    }
    /**
     * Checks whether a user with the given username, email,
     * or phone number already exists.
     *
     * @param dto the registration data to check for username, email,
     *            and phone number uniqueness
     */
    private void validateUserNotExists(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException(AppConstants.USERNAME_EXISTS);
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException(AppConstants.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BadRequestException(AppConstants.PHONE_ALREADY_EXISTS);
        }
    }

    /**
     * Gets role from database.
     *
     * @param roleName role name
     * @return role entity
     */
    private RoleEntity getValidRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.ROLE_NOT_FOUND + roleName));
    }

    /**
     * Creates new user object.
     *
     * @param dto  user data
     * @param role role entity
     * @return user entity
     */
    private UserEntity createNewUser(RegisterRequestDTO dto, RoleEntity role) {


        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail().trim());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);


        log.debug("User object created successfully for username: {}", dto.getUsername());

        return user;
    }

    /**
     * Validates role during registration.
     * Assigns USER by default if no role is provided.
     * Allows ADMIN role only if logged-in user is admin.
     * Throws exception if unauthorized role assignment is attempted.
     */
    private RoleName resolveAndValidateRole(RoleName requestedRole) {

        log.info("Resolving role for request: {}", requestedRole);

        if (requestedRole == null || requestedRole == RoleName.USER) {
            return RoleName.USER;
        }

        boolean adminExists = userRepository.existsByRole_RoleName(RoleName.ADMIN);

        if (!adminExists) {
            log.warn("No admin found in system. Allow first admin creation.");
            return requestedRole;
        }

        String loggedInUsername = loggedInUserService.getUsername();
        log.info("Logged-in user has role assignment request: {}", loggedInUsername);

        UserEntity loggedInUser = userRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> {
                    log.error("Logged-in user not found in DB: {}", loggedInUsername);
                    return new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + loggedInUsername);
                });

        if (loggedInUser.getRole() == null || loggedInUser.getRole().getRoleName() != RoleName.ADMIN) {
            throw new BadRequestException(AppConstants.ONLY_ADMIN_ALLOWED);
        }

        log.info("Admin authorization successful. Assigning role: {}", requestedRole);
        return requestedRole;
    }


    /**
     * Fetch users based on filters.
     *
     * @param username    username
     * @param email       email
     * @param phoneNumber phone number
     * @return list of users
     */
    @Override
    public List<UserResponseDTO> getUsers(String username, String email, String phoneNumber) {
        if (StringUtils.hasText(username)) {
            return List.of(getUserByUsername(username));
        }

        if (StringUtils.hasText(email)) {
            return List.of(getUserByEmail(email));
        }

        if (StringUtils.hasText(phoneNumber)) {
            return List.of(getUserByPhoneNumber(phoneNumber));
        }

        return getAllUsers();
    }

    /**
     * Get user by username.
     *
     * @param username username
     * @return user details
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = CacheConstant.USERNAME_USER_KEY)
    public UserResponseDTO getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + username));

        return userMapper.toDTO(user);
    }

    /**
     * Get user by email.
     *
     * @param email get user by email
     * @return user details
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstant.USERS, key = CacheConstant.EMAIL_USER_KEY)
    public UserResponseDTO getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + email));

        return userMapper.toDTO(user);
    }

    /**
     * get user by phoneNumber
     *
     * @param phoneNumber  get user by phoneNumber
     * @return user details
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstant.USERS, key = CacheConstant.PHONE_USER_KEY)
    public UserResponseDTO getUserByPhoneNumber(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + phoneNumber));

        return userMapper.toDTO(user);
    }

    /**
     * Get all users.
     *
     * @return list of users
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstant.USERS, key =CacheConstant.ALL_USERS_KEY)
    public List<UserResponseDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Get current logged-in user profile.
     *
     * @return user details
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getMyProfile() {
        String username = loggedInUserService.getUsername();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + username));

        return userMapper.toDTO(user);
    }

    private UserEntity applyUpdates(UserEntity user, UpdateUserDTO dto) {
        if (StringUtils.hasText(dto.getEmail())) {
            if (!dto.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException(AppConstants.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(dto.getEmail().trim());
        }

        if (StringUtils.hasText(dto.getPhoneNumber())) {
            String newPhone = dto.getPhoneNumber().trim();

            if (!newPhone.equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(newPhone)) {
                throw new BadRequestException(AppConstants.PHONE_ALREADY_EXISTS);
            }

            user.setPhoneNumber(newPhone);
        }

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Update user by username.
     *
     * @param username username
     * @param dto   update data
     * @return updated user
     */
    @Override
    @Transactional
    @Caching(
            put = {@CachePut(cacheNames = CacheConstant.USERS, key = CacheConstant.USERNAME_USER_KEY)},
            evict = {@CacheEvict(cacheNames = CacheConstant.USERS, key =CacheConstant.ALL_USERS_KEY)}
    )
    public UserResponseDTO updateUserByUsername(String username, UpdateUserDTO dto) {
        UserEntity existingUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + username));

        UserEntity updatedUser = applyUpdates(existingUser, dto);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    @Caching(
            put = {@CachePut(cacheNames = CacheConstant.USERS, key = CacheConstant.RESULT_USER_KEY)},
            evict = {@CacheEvict(cacheNames =CacheConstant.USERS, key = CacheConstant.ALL_USERS_KEY)}
    )
    public UserResponseDTO updateMyProfile(UpdateUserDTO dto) {
        String username = loggedInUserService.getUsername();

        UserEntity existingUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + username));

        UserEntity updatedUser = applyUpdates(existingUser, dto);
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Deletes user by username (admin only).
     *
     * @param username username
     */
    @Override
    @CacheEvict(cacheNames = CacheConstant.USERS, allEntries = true)
    public void deleteUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + username));

        userRepository.delete(user);
        log.info("User deleted successfully: {}", username);
    }



}