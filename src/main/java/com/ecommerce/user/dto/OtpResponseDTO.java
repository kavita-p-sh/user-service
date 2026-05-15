package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Response DTO for OTP APIs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponseDTO {

    private int status;
    private String message;
    private Long remainingSeconds;
}
