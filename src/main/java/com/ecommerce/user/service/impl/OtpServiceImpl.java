package com.ecommerce.user.service.impl;

import com.ecommerce.common.exception.*;
import com.ecommerce.common.util.CacheConstant;
import com.ecommerce.user.service.OtpGenerator;
import com.ecommerce.user.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service implementation for handling OTP generation,
 * verification, and security checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String UNKNOWN_MASKED_VALUE = "UNKNOWN";
    private static final String MASK_VALUE = "***";
    private static final String SHORT_MASK_VALUE = "****";
    private static final String EMAIL_SEPARATOR = "@";

    private static final int VISIBLE_PREFIX_LENGTH = 2;
    private static final int VISIBLE_SUFFIX_LENGTH = 2;
    private static final int MIN_LENGTH_FOR_PARTIAL_MASK = 4;


    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpGenerator otpGenerator;

    /**
     * @param uniqueKey unique identifier for OTP
     * @param ipAddress IP address of the user requesting OTP
     * @return generated OTP if successful, or a message if OTP already exists
     */
    @Override
    public String generateOtp(String uniqueKey, String ipAddress) {

        checkIpBlocked(ipAddress);
        checkIpRequestLimit(ipAddress);
        checkIdentityOtpRequestLimit(uniqueKey);

        String maskedKey = maskUniqueKey(uniqueKey);

        log.info("Generating OTP for key: {} from IP: {}", maskedKey, ipAddress);
        String otpKey = CacheConstant.OTP_PREFIX + uniqueKey;

        String otp = otpGenerator.generateOtp();

        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(otpKey, otp, CacheConstant.OTP_TTL_MINUTES, TimeUnit.MINUTES);

        if (!Boolean.TRUE.equals(created)) {
            Long remaining = redisTemplate.getExpire(otpKey, TimeUnit.SECONDS);
            throw new OtpAlreadySentException(
                    CacheConstant.OTP_ALREADY_SENT,
                    remaining != null ? remaining : CacheConstant.DEFAULT_TTL
            );
        }

        redisTemplate.delete(CacheConstant.VERIFY_COUNT_PREFIX + uniqueKey);
        log.info("OTP stored in cache for key: {} with TTL: {} minutes",
                maskedKey, CacheConstant.OTP_TTL_MINUTES);

        return CacheConstant.OTP_SENT;
    }

    /**
     * Tracks OTP generation count per identity such as email or phone number.
     *
     * This prevents attackers from requesting too many OTPs for the same
     * phone/email even if they rotate IP addresses.
     *
     * @param uniqueKey email or phone number for which OTP is requested
     * @throws TooManyRequestsException if identity request limit is exceeded
     */
    private void checkIdentityOtpRequestLimit(String uniqueKey) {

        String maskedKey = maskUniqueKey(uniqueKey);

        String key = CacheConstant.OTP_COUNT_PREFIX + uniqueKey;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(
                    key,
                    CacheConstant.OTP_REQUEST_IDENTITY_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        }

        log.info("OTP request count {} for identity: {}", count, maskedKey);

        if (count != null && count > CacheConstant.MAX_OTP_REQUEST_PER_IDENTITY) {
            log.error("Too many OTP requests for identity: {}", maskedKey);
            throw new TooManyRequestsException(
                    CacheConstant.TOO_MANY_OTP_REQUESTS_FOR_IDENTITY
            );
        }
    }


    /**
     * Verifies the entered OTP against the cached OTP.
     * @param uniqueKey unique identifier (email/phone)
     * @param enteredOtp  OTP entered by user
     * @param ipAddress client IP address
     * @return true if OTP is valid, false if not valid
     * @throws VerificationLimitExceededException if OTP expired or attempt limit exceeded
     */
    @Override
    public boolean verifyOtp(String uniqueKey, String enteredOtp, String ipAddress) {

        checkIpBlocked(ipAddress);
        checkIpVerifyLimit(ipAddress);
        checkIdentityVerifyLimit(uniqueKey);

        String maskedKey = maskUniqueKey(uniqueKey);

        log.info("Verifying OTP for key: {} from IP: {}", maskedKey, ipAddress);

        String otpKey = CacheConstant.OTP_PREFIX + uniqueKey;
        Object cachedOtp = redisTemplate.opsForValue().get(otpKey);

        if (cachedOtp == null) {
            log.warn("OTP expired or not found for key: {}", maskedKey);
            throw new OtpExpiredException(CacheConstant.OTP_EXPIRED);
        }

        if (cachedOtp.toString().equals(enteredOtp)) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(CacheConstant.VERIFY_COUNT_PREFIX + uniqueKey);

            log.info("OTP verified successfully for key: {}", maskedKey);
            return true;
        }

        log.warn("Invalid OTP entered for key: {}", maskedKey);
        return false;
    }

    private void checkIpVerifyLimit(String ip) {

        String key = CacheConstant.VERIFY_IP_COUNT_PREFIX + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, CacheConstant.OTP_ATTEMPT_TTL_MINUTES, TimeUnit.MINUTES);
        }

        log.info("Verify attempt count {} for IP: {}", count, ip);

        if (count != null && count > CacheConstant.MAX_VERIFY_REQUEST_PER_IP) {
            redisTemplate.opsForValue().set(
                    CacheConstant.IP_BLOCK_PREFIX + ip,
                    CacheConstant.BLOCKED_VALUE,
                    CacheConstant.IP_BLOCK_TTL_MINUTES,
                    TimeUnit.MINUTES
            );

            log.error("IP blocked due to too many verify attempts: {}", ip);
            throw new TooManyRequestsException(CacheConstant.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Fetches the currently stored OTP from Redis.
     * @param uniqueKey unique identifier email/phone
     * @return cached OTP or null if not found
     */
    @Override
    public String getCachedOtp(String uniqueKey) {

        String maskedKey = maskUniqueKey(uniqueKey);

        log.debug("Fetching cached OTP for key: {}", maskedKey);

        Object value = redisTemplate.opsForValue().get(CacheConstant.OTP_PREFIX + uniqueKey);
        return value != null ? value.toString() : null;
    }

    /**
     * Returns remaining time (TTL) for the OTP in seconds.
     * @param uniqueKey unique identifier (email/phone)
     * @return remaining time in seconds, -1 if not available
     */
    @Override
    public long getOtpRemainingSeconds(String uniqueKey) {
        Long ttl = redisTemplate.getExpire(
                CacheConstant.OTP_PREFIX + uniqueKey,
                TimeUnit.SECONDS);

        String maskedKey = maskUniqueKey(uniqueKey);

        log.debug("OTP TTL for key: {} is {} seconds", maskedKey, ttl);

        if (ttl == null) {
            return -1;
        }
        return ttl;
    }

    /**
     * Checks whether the given IP address is blocked.
     * @param ip client IP address
     * @throws IpBlockedException if IP is blocked
     */

    private void checkIpBlocked(String ip) {

        Object blocked = redisTemplate.opsForValue()
                .get(CacheConstant.IP_BLOCK_PREFIX + ip);

        if (blocked != null) {
            log.error("Blocked IP tried to request OTP: {}", ip);
            throw new IpBlockedException(CacheConstant.IP_BLOCKED);
        }
    }

    /**
     * Tracks OTP request count per IP and applies rate limiting.
     * @param ip client IP address
     * @throws TooManyRequestsException if request limit exceeded
     */
    private void checkIpRequestLimit(String ip) {

        String key = CacheConstant.OTP_IP_COUNT_PREFIX + ip;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, CacheConstant.OTP_REQUEST_TTL_MINUTES, TimeUnit.MINUTES);
        }
        log.info("OTP request count {} for IP: {}", count, ip);


        if (count != null && count > CacheConstant.MAX_OTP_REQUEST_PER_IP) {
            redisTemplate.opsForValue().set(
                    CacheConstant.IP_BLOCK_PREFIX + ip,
                    CacheConstant.BLOCKED_VALUE,
                    CacheConstant.IP_BLOCK_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
            log.error("IP blocked due to too many requests: {}", ip);
            throw new TooManyRequestsException(CacheConstant.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Tracks OTP verification attempts per identity such as email or phone number.
     *
     * This prevents brute-force attacks for the same victim identity
     * even if requests come from multiple IP addresses.
     *
     * @param uniqueKey email or phone number
     * @throws VerificationLimitExceededException if verify attempt limit is exceeded
     */
    private void checkIdentityVerifyLimit(String uniqueKey) {

        String maskedKey = maskUniqueKey(uniqueKey);
        String key = CacheConstant.VERIFY_COUNT_PREFIX + uniqueKey;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(
                    key,
                    CacheConstant.VERIFY_IDENTITY_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        }

        log.info("OTP verify count {} for identity: {}", count, maskedKey);

        if (count != null && count > CacheConstant.MAX_VERIFY_REQUEST_PER_IDENTITY) {
            redisTemplate.delete(CacheConstant.OTP_PREFIX + uniqueKey);
            redisTemplate.delete(key);

            log.error("Verification limit exceeded for identity: {}", maskedKey);

            throw new VerificationLimitExceededException(
                    CacheConstant.TOO_MANY_VERIFY_ATTEMPTS_FOR_IDENTITY
            );
        }
    }

    /**
     * Masks email or phone number before writing it to logs.
     *
     * @param uniqueKey email or phone number
     * @return masked value safe for logs
     */
    private String maskUniqueKey(String uniqueKey) {

        if (uniqueKey == null || uniqueKey.isBlank()) {
            return UNKNOWN_MASKED_VALUE;
        }

        if (uniqueKey.contains(EMAIL_SEPARATOR)) {
            String[] parts = uniqueKey.split(EMAIL_SEPARATOR, 2);
            String localPart = parts[0];
            String domain = parts[1];

            if (localPart.length() <= VISIBLE_PREFIX_LENGTH) {
                return MASK_VALUE + EMAIL_SEPARATOR + domain;
            }

            return localPart.substring(0, VISIBLE_PREFIX_LENGTH)
                    + MASK_VALUE
                    + EMAIL_SEPARATOR
                    + domain;
        }

        if (uniqueKey.length() <= MIN_LENGTH_FOR_PARTIAL_MASK) {
            return SHORT_MASK_VALUE;
        }

        return uniqueKey.substring(0, VISIBLE_PREFIX_LENGTH)
                + MASK_VALUE
                + uniqueKey.substring(uniqueKey.length() - VISIBLE_SUFFIX_LENGTH);
    }
}
