package com.ecommerce.user.service.impl;


import com.ecommerce.common.entity.UserEntity;
import com.ecommerce.common.repository.UserRepository;
import com.ecommerce.common.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of UserDetailsService used by Spring Security.
 *
 * This class loads user details from database during authentication.
 * It helps Spring Security to verify username and password.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username.
     *
     * @param username the username identifying the user whose data is required.
     * @return user details for Spring Security authentication
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Trying to load user with username: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException(AppConstants.USER_NOT_FOUND + username);
                });

        log.info("User found: {}, role: {}", user.getUsername(), user.getRole().getRoleName());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name()))
        );
    }
}