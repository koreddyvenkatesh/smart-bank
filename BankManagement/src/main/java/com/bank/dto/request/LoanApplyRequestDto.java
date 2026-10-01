package com.bank.dto.request;

import lombok.Data;

@Data
public class LoanApplyRequestDto {

	private String loanType;
	private double amount;
	private int tenureMonths;
}
