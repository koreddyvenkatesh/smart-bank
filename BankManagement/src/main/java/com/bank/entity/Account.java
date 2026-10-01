package com.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String accountNumber;
	
	@Column(nullable=false)
	private String accountType;
	
	@Column(nullable = false)
	private double balance=0.0;
	
	@Column(nullable = false)
	private String status="PENDING";
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="customer_id",nullable = false)
	private Customer customer;
	
}
