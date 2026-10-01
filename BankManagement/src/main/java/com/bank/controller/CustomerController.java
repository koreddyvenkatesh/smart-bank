package com.bank.controller;

import com.bank.dto.request.LoanApplyRequestDto;
import com.bank.dto.request.TransferRequestDto;
import com.bank.dto.response.ApiResponseDto;

import com.bank.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final BankService bankService;

    public CustomerController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/profile")
    public ResponseEntity getProfile(Authentication authentication) {
        return ResponseEntity.ok(bankService.getCustomerProfile(authentication.getName()));
    }

    @PostMapping("/accounts/{accountNumber}/transfer")
    public ResponseEntity transferFunds(
            @PathVariable String accountNumber,
            @RequestBody TransferRequestDto request) {
        
        ApiResponseDto response = bankService.transferFunds(accountNumber, request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/accounts/{accountNumber}/loans/apply")
    public ResponseEntity applyForLoan(
            @PathVariable String accountNumber,
            @RequestBody LoanApplyRequestDto request) {
        
        ApiResponseDto response = bankService.applyForLoan(request, accountNumber);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
