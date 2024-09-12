package com.PrathihasProjects.PrathihasSplitwise.dto;

public class OtpDTO {

    public String email;

    public String otp;

    public OtpDTO() {}
    public OtpDTO(String email, String otp)
    {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
