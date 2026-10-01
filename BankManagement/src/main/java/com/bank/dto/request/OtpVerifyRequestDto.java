package com.bank.dto.request;

import lombok.Data;

@Data
public class OtpVerifyRequestDto {

	private String emailOrAccountNumber;
    private String otpCode;
}
