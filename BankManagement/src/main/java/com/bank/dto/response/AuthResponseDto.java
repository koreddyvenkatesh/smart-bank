package com.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {

	private String token;
	private String message;
	private String role;
}
