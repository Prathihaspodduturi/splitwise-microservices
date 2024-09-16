package com.PrathihasProjects.PrathihasSplitwise.Controller;

import com.PrathihasProjects.PrathihasSplitwise.dao.UserDAOImpl;
import com.PrathihasProjects.PrathihasSplitwise.dto.OtpDTO;
import com.PrathihasProjects.PrathihasSplitwise.dto.UsernameOrEmailDTO;
import com.PrathihasProjects.PrathihasSplitwise.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.concurrent.TimeUnit;

import java.security.SecureRandom;

@RestController
@CrossOrigin
public class OtpController {

    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final StreamBridge streamBridge;
    private final UserDAOImpl theUserDAOImpl;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public OtpController(UserDAOImpl theUserDAOImpl, StreamBridge streamBridge) {
        this.theUserDAOImpl = theUserDAOImpl;
        this.streamBridge = streamBridge;
    }

    @PostMapping("/splitwise/reset-password/otp")
    public ResponseEntity<?> OtpGenerate(@RequestBody UsernameOrEmailDTO usernameOrEmailDTO)
    {
        try
        {
            String usernameOrEmail = usernameOrEmailDTO.getUsernameOrEmail();
            log.info("Received request for OTP generation for: {}", usernameOrEmail);

            // Try to find the user by username
            User findUser = theUserDAOImpl.findUserByName(usernameOrEmail);

            // If not found by username, try to find by email
            if (findUser == null) {
                log.info("User not found by username, trying by email: {}", usernameOrEmail);
                findUser = theUserDAOImpl.findUserByEmail(usernameOrEmail);
            }

            // If the user is still not found, return an error
            if (findUser == null) {
                log.error("No user found with username or email: {}", usernameOrEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The Username or Email is not linked to any account");
            }

            // Extract email and generate OTP
            String email = findUser.getEmail();
            String redisKey = email; // Key is just the email
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String otpAndCount = ops.get(redisKey);

            String otp;
            int requestCount;

            if (otpAndCount == null) {
                // No previous OTP or count found, initialize both
                otp = null;
                requestCount = 0;
            } else {
                // Split the value into OTP and request count
                String[] parts = otpAndCount.split(":");
                otp = parts[0]; // Previous OTP (not used further)
                requestCount = Integer.parseInt(parts[1]);
            }

            // Check if the request count exceeds the limit
            if (requestCount > 3) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many OTP requests. Please try again later.");
            }

            otp = generateOtp();
            requestCount++;
            // Send the OTP
            sendOtp(email, otp);

            String newOtpAndCount = otp + ":" + requestCount;
            ops.set(redisKey, newOtpAndCount, 1, TimeUnit.HOURS);

            log.info("OTP sent successfully to {}", email);
            return ResponseEntity.ok("Otp sent successfully to email");
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred, Please try again later!");
        }
    }


    private void sendOtp(String email, String otp)
    {
        OtpDTO otpDTO = new OtpDTO(email, otp);
        log.info("sending communication request for the details: {}", email);
        var result = streamBridge.send("sendOtp-out-0", otpDTO);
        log.info("Is the communication request successfully processed?: {}", result);
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);  // Generate a number between 100000 and 999999
        return String.valueOf(otp);  // Convert the integer OTP to a String
    }

}
