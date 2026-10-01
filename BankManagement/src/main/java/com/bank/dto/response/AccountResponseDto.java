package com.bank.dto.response;

import lombok.Data;

@Data

public class AccountResponseDto {

	private String accountNumber;
	private String accountType;
	private double balance;
	private String status;
}
