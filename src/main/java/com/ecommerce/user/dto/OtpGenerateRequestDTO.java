package com.ecommerce.user.dto;


import com.ecommerce.common.util.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for OTP generation.
 * Accepts email address or phone number as key.
 */
@Data
public class OtpGenerateRequestDTO {

    @NotBlank(message = "{otp.key.required}")
    @Size(max = 128, message = "{otp.key.size}")
    @Pattern(
            regexp = RegexConstant.OTP_KEY,
            message ="{otp.key.invalid}"
    )
    private String key;
}