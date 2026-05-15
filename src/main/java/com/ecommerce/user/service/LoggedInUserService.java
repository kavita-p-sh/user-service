package com.ecommerce.user.service;
import com.ecommerce.user.dto.LoggedInUserDTO;

/**
 * Service interface to get details of logged-in user.
 */
public interface LoggedInUserService {


    /**
     * Gets current logged-in user details.
     *
     * @return logged-in user data
     */
    LoggedInUserDTO getCurrentUser();

    /**
     * Gets username of logged-in user.
     *
     * @return username
     */
    String getUsername();

    /**
     * Checks if logged-in user is admin.
     *
     * @return true if admin, else false
     */
    boolean isAdmin();

    String getRole();
}
