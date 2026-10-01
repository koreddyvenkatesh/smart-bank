package com.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.request.AuthRequestDto;
import com.bank.dto.request.OtpVerifyRequestDto;
import com.bank.dto.request.PasswordResetRequestDto;
import com.bank.dto.response.ApiResponseDto;
import com.bank.service.BankService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final BankService bankService;
	 public AuthController(BankService bankService) {
		 this.bankService=bankService;
	 }
	 
	 @PostMapping("/login")
	 public ResponseEntity login(@RequestBody AuthRequestDto authRequestDto){
		 
		 return ResponseEntity.ok(bankService.authenticateUser(authRequestDto));
	 }
	 
	 @PostMapping("/otp/send")
	 public ResponseEntity sendOtp(@RequestParam String identifier) {
		 return ResponseEntity.ok(bankService.generateAndSendOtp(identifier));
	 }
	 
	 @PostMapping("/otp/verify")
	 public ResponseEntity verifyOtp(@RequestBody OtpVerifyRequestDto otpVerifyRequestDto) {
		 boolean isValid=bankService.verifyOtp(otpVerifyRequestDto.getEmailOrAccountNumber(), otpVerifyRequestDto.getOtpCode());
		 if(isValid) {
			 return ResponseEntity.ok(new ApiResponseDto("Otp verified successfully",true));
		 }
		 
		 return ResponseEntity.badRequest().body(new ApiResponseDto("invalid or expired otp", false));
		 
	 }
	 
	 @PostMapping("/reset-password")
	 public ResponseEntity resetPassword(@RequestBody PasswordResetRequestDto request) {
	     try {
	         return ResponseEntity.ok(bankService.resetPassword(request));
	     } catch (RuntimeException e) {
	         return ResponseEntity.badRequest().body(new ApiResponseDto(e.getMessage(), false));
	     }
	 }

}
