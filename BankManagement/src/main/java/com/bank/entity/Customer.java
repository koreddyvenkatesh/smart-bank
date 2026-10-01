package com.bank.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="customers")
public class Customer {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private String fullName;
	
	@Column(nullable=false,unique=true)
	private String email;
	
	@Column(nullable=false,unique=true)
	private String mobile;
	
	private String password;
	
	private String tempPassword;
	
	private boolean tempPasswordActive=false;
	
	@Column(nullable=false)
	private String role="ROLE_CUSTOMER";
	
	private String kycStatus="PENDING";
	
	private boolean active=false;
	
	@OneToMany(mappedBy = "customer" ,cascade=CascadeType.ALL,fetch=FetchType.LAZY)
	@ToString.Exclude
	private List<Account> accounts=new ArrayList<>();
	

}
