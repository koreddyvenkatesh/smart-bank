package com.bank.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AdminCustomerResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String kycStatus;
    private boolean active;
    private List<AccountResponseDto> accounts;
}