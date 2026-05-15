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

        log.info("Generating OTP for key: {} from IP: {}", uniqueKey, ipAddress);
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

        log.info("OTP stored in cache for key: {} with TTL: {} minutes",
                uniqueKey, CacheConstant.OTP_TTL_MINUTES);

        return CacheConstant.OTP_SENT;
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
        log.info("Verifying OTP for key: {} from IP: {}", uniqueKey, ipAddress);
        String otpKey = CacheConstant.OTP_PREFIX + uniqueKey;

        Object cachedOtp = redisTemplate.opsForValue().get(otpKey);

        if (cachedOtp == null) {
            log.warn("OTP expired or not found for key: {}", uniqueKey);
            throw new OtpExpiredException(CacheConstant.OTP_EXPIRED);
        }


        String attemptKey = CacheConstant.OTP_ATTEMPT_PREFIX + uniqueKey;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, CacheConstant.OTP_ATTEMPT_TTL_MINUTES, TimeUnit.MINUTES);
        }
        log.info("OTP verification attempt {} for key: {}", attempts, uniqueKey);


        if (attempts != null && attempts > CacheConstant.MAX_VERIFY_ATTEMPTS) {
            log.error("Verification limit exceeded for key: {}", uniqueKey);

            redisTemplate.delete(otpKey);
            throw new VerificationLimitExceededException(CacheConstant.VERIFICATION_LIMIT_EXCEED);
        }

        if (cachedOtp.toString().equals(enteredOtp)) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            log.info("OTP verified successfully for key: {}", uniqueKey);

            return true;
        }

        log.warn("Invalid OTP entered for key: {}", uniqueKey);
        return false;
    }

    private void checkIpVerifyLimit(String ip) {

        String key = CacheConstant.VERIFY_COUNT_PREFIX + ip;

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
            throw new TooManyRequestsException("Too many OTP verification attempts from this IP");
        }
    }

    /**
     * Fetches the currently stored OTP from Redis.
     * @param uniqueKey unique identifier email/phone
     * @return cached OTP or null if not found
     */
    @Override
    public String getCachedOtp(String uniqueKey) {
        log.debug("Fetching cached OTP for key: {}", uniqueKey);

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
        log.debug("OTP TTL for key: {} is {} seconds", uniqueKey, ttl);

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
        String key = CacheConstant.OTP_COUNT_PREFIX + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, CacheConstant.OTP_ATTEMPT_TTL_MINUTES, TimeUnit.MINUTES);
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
}
