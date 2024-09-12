package com.PrathihasProjects.PrathihasSplitwise.Controller;

import com.PrathihasProjects.PrathihasSplitwise.dao.UserDAOImpl;
import com.PrathihasProjects.PrathihasSplitwise.dto.OtpDTO;
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

    @PostMapping("/splitwise/reset-password/Otp")
    public ResponseEntity<?> OtpGenerate(@RequestBody String usernameOrEmail)
    {
        try
        {
            User findUser = theUserDAOImpl.findUserByName(usernameOrEmail);

            String email = usernameOrEmail;
            if(findUser != null)
                email = theUserDAOImpl.findEmailOfUser(usernameOrEmail);
            else
            {
                findUser = theUserDAOImpl.findUserByEmail(usernameOrEmail);
                if(findUser == null)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The Username or Email is not linked to any account");
            }

            String otp = generateOtp();
            sendOtp(email, otp);

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
        var result = streamBridge.send("sendOtp-out-0", email);
        log.info("Is the communication request successfully processed?: {}", result);
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);  // Generate a number between 100000 and 999999
        return String.valueOf(otp);  // Convert the integer OTP to a String
    }

}
