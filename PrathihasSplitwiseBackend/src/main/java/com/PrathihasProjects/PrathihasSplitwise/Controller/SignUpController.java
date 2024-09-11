package com.PrathihasProjects.PrathihasSplitwise.Controller;

import com.PrathihasProjects.PrathihasSplitwise.dao.UserDAOImpl;
import com.PrathihasProjects.PrathihasSplitwise.dto.AccountCreatedDto;
import com.PrathihasProjects.PrathihasSplitwise.entity.User;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController
@CrossOrigin
public class SignUpController {

    private static final Logger log = LoggerFactory.getLogger(SignUpController.class);

    private final StreamBridge streamBridge;
    private final UserDAOImpl theUserDAOImpl;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignUpController(UserDAOImpl theUserDAOImpl, StreamBridge streamBridge) {
        this.theUserDAOImpl = theUserDAOImpl;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.streamBridge = streamBridge;
    }

    @PostMapping("/splitwise/signup")
    public ResponseEntity<?> signupController(@RequestBody User newUser)
    {
        try {
            User findUser = theUserDAOImpl.findUserByName(newUser.getUsername());

            if(findUser != null){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists or username is already taken");
            }

            findUser = theUserDAOImpl.findUserByEmail(newUser.getEmail());
            if(findUser != null){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use by other user");
            }

            String hashedPassword = passwordEncoder.encode(newUser.getPassword());
            newUser.setPassword(hashedPassword);

            theUserDAOImpl.save(newUser);

            User createdUser = theUserDAOImpl.findUserByName((newUser.getUsername()));
            sendCommunication(createdUser);

            return ResponseEntity.ok("Signup successfull");

        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred!");
        }
    }

    private void sendCommunication(User createdUser)
    {
        var accountCreatedDto = new AccountCreatedDto(createdUser.getUsername(), "prathihasamazon@gmail.com");
        log.info("sending communication request for the details: {}", accountCreatedDto);
        var result = streamBridge.send("sendCommunication-out-0", accountCreatedDto);
        log.info("Is the communication request successfully processed?: {}", result);
    }
}
