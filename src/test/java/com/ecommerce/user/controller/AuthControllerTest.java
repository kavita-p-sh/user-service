package com.ecommerce.user.controller;

import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.util.AppConstants;
import com.ecommerce.common.util.CacheConstant;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthController.
 *
 * This class verifies authentication-related APIs:
 * - User registration
 * - User login
 * - User logout
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Mock
    private OtpService otpService;

    @Mock
    private HttpServletRequest httpServletRequest;

    /**
     * Tests successful user registration.
     * - HTTP status is 201 (Created)
     * - Returned username matches expected value
     */
    @Test
    void  register_success(){
        RegisterRequestDTO request=new RegisterRequestDTO();
        request.setUsername("RamPatel");

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("RamPatel");

        when(authService.register(request)).thenReturn(response);

        ResponseEntity<UserResponseDTO> result = authController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("RamPatel", result.getBody().getUsername());
        verify(authService).register(request);
    }

    /**
     * Tests Register Data is valid or not.
     */
    @Test
    void register_invalidData() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("RamPatel");

        when(authService.register(request))
                .thenThrow(new BadRequestException("Invalid registration data"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authController.register(request)
        );

        assertEquals("Invalid registration data", exception.getMessage());
        verify(authService).register(request);
    }

    /**
     *
     */
    @Test
    void register_DuplicateUser() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("RamPatel");

        when(authService.register(request))
                .thenThrow(new BadRequestException("Username already exists"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authController.register(request)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(authService).register(request);
    }
    /**
     * Successfully login
     * HTTP status is 200 ok
     * JWT token is returned
     */
    @Test
    void login_success()
    {
        LoginRequestDTO request= new LoginRequestDTO();
        request.setUsername("Ram Patel");

        when(authService.login(request)).thenReturn("mock-token");

        ResponseEntity<String> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("mock-token", result.getBody());


        verify(authService).login(request);

    }

    /**
     * Tests Login Credential is Valid.
     */
    @Test
    void login_invalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("RamPatel");

        when(authService.login(request))
                .thenThrow(new BadRequestException("Invalid username or password"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authController.login(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(authService).login(request);
    }

    /**
     * Test case: OTP should be generated successfully.
     *
     * Verifies:
     * - Correct IP is extracted from request
     * - Service method is called with correct parameters
     * - Response status is  OK
     * - Response body contains success message
     */
    @Test
    void shouldGenerateOtpSuccessfully() {
        OtpGenerateRequestDTO request = new OtpGenerateRequestDTO();
        request.setKey("test135@gmail.com");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(otpService.generateOtp("test135@gmail.com", "127.0.0.1"))
                .thenReturn("123456");

        ResponseEntity<String> result = authController.generateOtp(request, httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(CacheConstant.OTP_SENT, result.getBody());
        verify(otpService).generateOtp("test135@gmail.com", "127.0.0.1");
    }

    /**
     * Test case: OTP verification should succeed when OTP is valid.
     *
     * Verifies:
     * - Correct IP is extracted
     * - Service returns true for valid OTP
     * - Response status is OK
     * - Success message is returned
     */

    @Test
    void verifyOtp_success() {
        OtpVerifyRequestDTO request = new OtpVerifyRequestDTO();
        request.setKey("test135@gmail.com");
        request.setOtp("123456");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(otpService.verifyOtp("test135@gmail.com", "123456", "127.0.0.1"))
                .thenReturn(true);

        ResponseEntity<String> result = authController.verifyOtp(request, httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(CacheConstant.OTP_VERIFIED, result.getBody());
        verify(otpService).verifyOtp("test135@gmail.com", "123456", "127.0.0.1");
    }

    @Test
    void verifyOtp_invalidOtp() {
        OtpVerifyRequestDTO request = new OtpVerifyRequestDTO();
        request.setKey("test135@gmail.com");
        request.setOtp("999999");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(otpService.verifyOtp("test135@gmail.com", "999999", "127.0.0.1"))
                .thenReturn(false);

        ResponseEntity<String> result = authController.verifyOtp(request, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(CacheConstant.INVALID_OTP, result.getBody());
        verify(otpService).verifyOtp("test135@gmail.com", "999999", "127.0.0.1");
    }
    /**
     * Test Logout
     * -HTTP status is (OK)
     * -Logout success message is returned
     */

    @Test
    void logout_success() {
        ResponseEntity<String> result = authController.logout();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(AppConstants.LOGOUT_SUCCESS, result.getBody());
    }




}
