package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.Loan;
import java.util.List;



public interface LoanRepository extends JpaRepository<Loan, Long> {
	
	List<Loan> findByAccountNumber(String accountNumber);

	List<Loan> findByStatus(String status);
}
