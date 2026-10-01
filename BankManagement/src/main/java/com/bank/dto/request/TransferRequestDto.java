package com.bank.dto.request;

import lombok.Data;

@Data
public class TransferRequestDto {

	private String toAccount;
	private double amount;
}
