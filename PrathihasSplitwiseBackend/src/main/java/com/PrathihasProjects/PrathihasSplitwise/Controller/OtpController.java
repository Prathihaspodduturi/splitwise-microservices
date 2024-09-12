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

import java.security.SecureRandom;

@RestController
@CrossOrigin
public class OtpController {

    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final StreamBridge streamBridge;
    private final UserDAOImpl theUserDAOImpl;

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
            String otp = generateOtp();

            // Send the OTP
            sendOtp(email, otp);

            log.info("OTP sent successfully to {}", email);
            return ResponseEntity.ok("Otp sent successfully to email");
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred!");
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
