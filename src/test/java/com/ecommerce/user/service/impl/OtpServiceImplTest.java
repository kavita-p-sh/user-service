package com.ecommerce.user.service.impl;

import com.ecommerce.common.exception.*;
import com.ecommerce.user.service.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for {@link OtpServiceImpl}.
 *
 * <p>This class verifies OTP generation and verification scenarios such as:
 * successful OTP generation, duplicate OTP generation, correct OTP verification,
 * incorrect OTP verification, expired OTP handling, request limit validation,
 * and IP block validation.</p>
 *
 * <p>Redis operations are mocked because OTP values, request counters,
 * verification counters, and blocked IP status are stored in Redis.</p>
 */
class OtpServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private OtpGenerator otpGenerator;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateOtp_shouldGenerateSuccessfully_whenOtpNotAlreadySent() {
        String identity = "test@gmail.com";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return null;
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(otpGenerator.generateOtp()).thenReturn("123456");

        assertDoesNotThrow(() -> otpService.generateOtp(identity, ipAddress));

        verify(valueOperations, atLeastOnce()).set(anyString(), eq("123456"), any(Duration.class));
    }

    @Test
    void generateOtp_shouldThrowOtpAlreadySentException_whenOtpAlreadySent() {
        String identity = "test@gmail.com";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return "123456";
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(1L);

        assertThrows(OtpAlreadySentException.class,
                () -> otpService.generateOtp(identity, ipAddress));
    }

    @Test
    void verifyOtp_shouldVerifySuccessfully_whenOtpIsCorrect() {
        String identity = "test@gmail.com";
        String otp = "123456";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return "123456";
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(1L);

        assertDoesNotThrow(() -> otpService.verifyOtp(identity, otp, ipAddress));

        verify(redisTemplate, atLeastOnce()).delete(anyString());
    }

    @Test
    void verifyOtp_shouldThrowInvalidOtpException_whenOtpIsIncorrect() {
        String identity = "test@gmail.com";
        String otp = "000000";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return "123456";
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(1L);

        assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp(identity, otp, ipAddress));
    }

    @Test
    void verifyOtp_shouldThrowOtpExpiredException_whenOtpExpired() {
        String identity = "test@gmail.com";
        String otp = "123456";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return null;
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(1L);

        assertThrows(OtpExpiredException.class,
                () -> otpService.verifyOtp(identity, otp, ipAddress));
    }

    @Test
    void verifyOtp_shouldThrowTooManyRequestsException_whenVerifyLimitExceeded() {
        String identity = "test@gmail.com";
        String otp = "000000";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return "123456";
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(6L);

        assertThrows(TooManyRequestsException.class,
                () -> otpService.verifyOtp(identity, otp, ipAddress));
    }

    @Test
    void generateOtp_shouldThrowTooManyRequestsException_whenIpRequestLimitExceeded() {
        String identity = "test@gmail.com";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return null;
            }

            if (key.contains(identity)) {
                return null;
            }

            return null;
        });

        when(valueOperations.increment(anyString())).thenReturn(6L);

        assertThrows(TooManyRequestsException.class,
                () -> otpService.generateOtp(identity, ipAddress));
    }

    @Test
    void generateOtp_shouldThrowIpBlockedException_whenIpAlreadyBlocked() {
        String identity = "test@gmail.com";
        String ipAddress = "127.0.0.1";

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);

            if (key.toLowerCase().contains("blocked")) {
                return "BLOCKED";
            }

            return null;
        });

        assertThrows(IpBlockedException.class,
                () -> otpService.generateOtp(identity, ipAddress));
    }
}