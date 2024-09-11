package com.PrathihasProjects.PrathihasSplitwise.services;

import java.util.regex.Pattern;

public class EmailValidator {

    // Regex pattern for validating email format
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + // Local part
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; // Domain part

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
}
