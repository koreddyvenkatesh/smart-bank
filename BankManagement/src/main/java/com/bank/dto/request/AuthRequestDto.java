package com.bank.dto.request;

import lombok.Data;

@Data
public class AuthRequestDto {

	private String emailOrAccountNumber;
	private String password;
}
