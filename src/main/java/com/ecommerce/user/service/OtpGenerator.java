package com.ecommerce.user.service;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;
/**
 * Utility component responsible for generating one-time passwords (OTP).
 * This class uses {@link SecureRandom} to generate a secure 6-digit numeric OTP.
 * The generated OTP can be used for verification flows such as user registration,
 * login verification, password reset, or phone/email verification.
 */


@Component
public class OtpGenerator {

    /**
     * Secure random generator used for creating OTP values.
     */
    private final SecureRandom secureRandom=new SecureRandom();

    public  String generateOtp()
    {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }


}
