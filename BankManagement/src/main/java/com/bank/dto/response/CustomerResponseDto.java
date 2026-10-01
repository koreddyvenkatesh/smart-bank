package com.bank.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class CustomerResponseDto {

	private String fullName;
	private String email;
	private String mobile;
	private String kycStatus;
	private List<AccountResponseDto> accounts;
}
