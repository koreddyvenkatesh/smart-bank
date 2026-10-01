package com.bank.dto.request;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String emailOrAccountNumber;
    private String otpCode;
    private String newPassword;
}
