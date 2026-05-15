package com.ecommerce.user.controller;

import com.ecommerce.common.util.AppConstants;
import com.ecommerce.common.util.CacheConstant;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Handles authentication APIs like login, register, profile and logout.
 */
@Slf4j

@Tag(name = "Auth API", description = "Authentication APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /**
     * Register new user.
     */
    @PostMapping("/register")
    @Operation(summary = "Register user",description = "Registers a new user")

    @ApiResponses({@ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        log.info("Register request received for username: {}", dto.getUsername());

        UserResponseDTO response = authService.register(dto);

        log.info("User registered successfully: {}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user and return token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login user" ,description = "Authenticates user and returns JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO dto) {
        log.info("Login request for username: {}", dto.getUsername());

        String token = authService.login(dto);

        log.info("Login successfully for username: {}", dto.getUsername());

        return ResponseEntity.ok(token);
    }

    /**
     * Generates a new OTP for the given key if an active OTP does not already exist.
     * Also checks IP-based request limit before generating the OTP.
     */
    @Operation(
            summary = "Generate OTP",
            description = "Generates a new OTP for the given key"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP generated successfully"),
            @ApiResponse(responseCode = "400", description = "OTP already exists"),
            @ApiResponse(responseCode = "429", description = "Too many OTP requests from this IP")
    })
    @PostMapping("/otp/generate")
    public ResponseEntity<String> generateOtp(@Valid @RequestBody OtpGenerateRequestDTO requestDto,
                                              HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String key = requestDto.getKey();

        log.info("OTP generation request received for key: {} from IP: {}", key, ip);

        otpService.generateOtp(requestDto.getKey(), ip);

        log.info("OTP generated successfully for key: {} from IP: {}", key, ip);

        return ResponseEntity.ok(CacheConstant.OTP_SENT);
    }

    /**
     * Verifies the entered OTP for the given key.
     */
    @Operation(
            summary = "Verify OTP",
            description = "Verifies the OTP entered by the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "expired OTP")
    })
    @PostMapping("/otp/verify")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO requestDto,
                                            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String key = requestDto.getKey();


        log.info("OTP verification request received for key: {} from IP: {}", key, ip);

        boolean result = otpService.verifyOtp(requestDto.getKey(), requestDto.getOtp(), ip);

        if (result) {
            log.info("OTP verified successfully for key: {} from IP: {}", key, ip);
            return ResponseEntity.ok(CacheConstant.OTP_VERIFIED);
        }
        log.warn("Invalid OTP attempt for key: {} from IP: {}", key, ip);
        return ResponseEntity.badRequest().body(CacheConstant.INVALID_OTP);
    }

    /**
     * Logout API for JWT authentication.
     * Since JWT is stateless, server does not store or invalidate tokens.
     * Logout is handled by removing the token on client side.
     * For better security, token blacklist can be used.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the authenticated user")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_ADMIN, AppConstants.ROLE_MANAGER})
    public ResponseEntity<String> logout() {

        log.info("Logout request received");
        return ResponseEntity.ok(AppConstants.LOGOUT_SUCCESS);
    }
}
