package com.ecommerce.user.service.impl;


import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.util.AppConstants;
import com.ecommerce.user.dto.LoggedInUserDTO;
import com.ecommerce.user.service.LoggedInUserService;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation to fetch details of the currently logged-in user
 * from Spring Security context.
 */

@Service
public class LoggedInUserServiceImpl implements LoggedInUserService {

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new UnauthorizedException(AppConstants.USER_UNAUTHENTICATED);
        }
        return auth;
    }

    /**
     * Retrieves the current Authentication object from SecurityContext.
     * Also validates whether the user is authenticated or not.
     *
     * @return Authentication object of logged-in user
     */
    @Override
    public LoggedInUserDTO getCurrentUser() {

        Authentication auth = getAuthentication();

        String username = auth.getName();

        List<String> roles = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (roles.isEmpty()) {
            throw new RuntimeException(AppConstants.ROLE_NOT_ASSIGNED);
        }

        String role = roles.get(0);

        return new LoggedInUserDTO(username, role);
    }

    /**
     * Fetches details of the currently logged-in user
     * including username and roles.
     *
     * @return LoggedInUserDTO containing username and roles
     */
    @Override
    public String getUsername() {
        return getAuthentication().getName();
    }

    /**
     * Checks whether the current user has ADMIN role.
     *
     * @return true if user is ADMIN, otherwise false
     */
    @Override
    public boolean isAdmin() {
        Authentication auth = getAuthentication();

        return auth.getAuthorities()
                .stream()
                .anyMatch(role -> role.getAuthority().equals(AppConstants.ROLE_ADMIN));
    }

    @Override
    public String getRole() {
        return getAuthentication().getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new RuntimeException(AppConstants.ROLE_NOT_ASSIGNED));
    }


}