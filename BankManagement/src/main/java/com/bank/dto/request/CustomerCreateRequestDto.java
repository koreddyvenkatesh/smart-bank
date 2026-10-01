package com.bank.dto.request;

import lombok.Data;

@Data
public class CustomerCreateRequestDto {

	private String fullName;
	private String email;
	private String mobile;
	private String initialAccountType;
}
