package com.bank.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminLoanResponseDto {

    private Long id;
    private String accountNumber;
    private String loanType;
    private double amount;
    private int tenureMonths;
    private double interestRate;
    private String status;
    private LocalDate appliedDate;
}	