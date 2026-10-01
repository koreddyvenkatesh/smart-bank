package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.Otp;

import java.util.Optional;


public interface OtpRepository extends JpaRepository<Otp, Long>{
	Optional<Otp> findByIdentifierAndOtpCode(String accountNumber, String otpCode);

}
