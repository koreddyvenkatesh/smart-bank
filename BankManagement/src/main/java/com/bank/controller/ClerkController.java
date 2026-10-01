package com.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.request.CustomerCreateRequestDto;
import com.bank.dto.response.ApiResponseDto;
import com.bank.service.BankService;

@RestController
@RequestMapping("/api/clerk")
public class ClerkController {

	private BankService bankService;
	
	public ClerkController(BankService bankService) {
		this.bankService=bankService;
	}
	
	@PostMapping("/customers/initiate")
	public ResponseEntity intiateCustomerResgistration(@RequestBody CustomerCreateRequestDto customerCreateRequestDto) {
	
		ApiResponseDto response=bankService.intiateAccountCreation(customerCreateRequestDto);
		if(response.isSuccess()) {
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.badRequest().body(response);	
	}
	
	
}
