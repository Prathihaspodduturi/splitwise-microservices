package com.prathihassplitwise.email.functions;

import com.prathihassplitwise.email.dto.AccountCreatedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class EmailFunctions {

    private static final Logger log = LoggerFactory.getLogger(EmailFunctions.class);

    @Bean
    public Function<AccountCreatedDto, Void> email() {

        return accountCreatedDto -> {
            log.info("sending message for account creation: "+accountCreatedDto.toString());
            return null;
        };

    }

}
