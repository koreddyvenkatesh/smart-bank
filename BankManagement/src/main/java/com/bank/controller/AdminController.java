package com.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.request.KycReviewRequest;
import com.bank.dto.response.ApiResponseDto;
import com.bank.service.BankService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private BankService bankService;
	
	public AdminController(BankService bankService) {
		this.bankService=bankService;
	}
	
	@PostMapping("customers/{customerId}/approve")
	public ResponseEntity approverKyc(@PathVariable Long customerId) {
		
		ApiResponseDto approveCustomerKyc = bankService.approveCustomerKyc(customerId);
	
		if(approveCustomerKyc.isSuccess()) {
			return ResponseEntity.ok(approveCustomerKyc);
		}
		
		return ResponseEntity.badRequest().body(approveCustomerKyc);
		
	}
	
	@PostMapping("customers/{customerId}/review")
	public ResponseEntity reviewKyc(@PathVariable Long customerId, @RequestBody KycReviewRequest request) {
	    
	    ApiResponseDto reviewResult = bankService.reviewCustomerKyc(customerId, request.getAction(), request.getReason());

	    if(reviewResult.isSuccess()) {
	        return ResponseEntity.ok(reviewResult);
	    }
	    
	    return ResponseEntity.badRequest().body(reviewResult);
	}
	
	@PostMapping("/loans/{loanId}/approve")
	public ResponseEntity approveLoan(@PathVariable Long loanId) {
		
		ApiResponseDto approveLoan = bankService.approveLoan(loanId);
		if(approveLoan.isSuccess()) {
			return ResponseEntity.ok(approveLoan);
		}
		
		return ResponseEntity.badRequest().body(approveLoan);
	}
	
	@GetMapping("/customers/pending")
	public ResponseEntity getPendingCustomers() {
	    return ResponseEntity.ok(bankService.getPendingCustomers());
	}

	@GetMapping("/loans/pending")
	public ResponseEntity getPendingLoans() {
	    return ResponseEntity.ok(bankService.getPendingLoans());
	}
	
}
