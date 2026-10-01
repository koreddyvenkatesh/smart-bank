package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.entity.Account;
import java.util.List;
import java.util.Optional;


public interface AccountRepository extends JpaRepository<Account,Long> {
	
	Optional<Account> findByAccountNumber(String accountNumber);

	@Query("SELECT a FROM Account a WHERE a.customer.id= :customerId")
	List<Account> findByCustomerId(@Param("customerId") Long customerId);
	List<Account> findByStatus(String status);
}
