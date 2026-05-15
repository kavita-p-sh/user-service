package com.ecommerce.user.service;


public interface OtpService {

    String generateOtp(String uniqueKey,String ipAddress);

    boolean verifyOtp(String uniqueKey, String enteredOtp, String ipAddress);

    String getCachedOtp(String uniqueKey);

    long getOtpRemainingSeconds(String uniqueKey);


}
