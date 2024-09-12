package com.PrathihasProjects.PrathihasSplitwise.dto;

public class OtpDTO {

    public String email;

    public String Otp;

    public OtpDTO(String email, String otp)
    {
        this.email = email;
        this.Otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return Otp;
    }

    public void setOtp(String otp) {
        Otp = otp;
    }
}
