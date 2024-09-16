package com.PrathihasProjects.PrathihasSplitwise.Controller;

import com.PrathihasProjects.PrathihasSplitwise.dto.OtpDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class OtpVerificationController {

    private static final Logger log = LoggerFactory.getLogger(OtpVerificationController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/splitwise/reset-password/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpDTO otpDTO) {
        try {
            String email = otpDTO.getEmail();
            String inputOtp = otpDTO.getOtp();

            // Key to retrieve OTP from Redis
            String redisKey = email;

            // Get the stored OTP and count from Redis
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String otpAndCount = ops.get(redisKey);

            if (otpAndCount == null) {
                log.error("OTP has expired or does not exist for email: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP has expired or is invalid.");
            }

            // Split the OTP and count
            String[] parts = otpAndCount.split(":");
            String storedOtp = parts[0];  // Stored OTP

            // Check if the input OTP matches the stored one
            if (!storedOtp.equals(inputOtp)) {
                log.error("Invalid OTP entered for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
            }

            // OTP matches, proceed with resetting password or next step
            log.info("OTP verified successfully for email: {}", email);

            // Optionally, you can remove the OTP from Redis after successful verification
            redisTemplate.delete(redisKey);

            return ResponseEntity.ok("OTP verified successfully.");
        } catch (Exception e) {
            log.error("Error occurred during OTP verification for email: {}", otpDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during OTP verification.");
        }
    }
}
