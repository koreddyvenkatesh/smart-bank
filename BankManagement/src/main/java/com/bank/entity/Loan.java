package com.bank.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="loans")
public class Loan {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String accountNumber;
	
	@Column(nullable=false)
	private String loanType;
	
	@Column(nullable=false)
	private double amount;
	
	@Column(nullable = false)
    private int tenureMonths;
	
	@Column(nullable=false)
	private double interestRate;
	
	@Column(nullable=false)
	private String status="PENDING";
	@Column(nullable=false)
	private LocalDate appliedDate;

}
