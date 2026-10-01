package com.bank.config;

import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;

    public CustomUserDetailsService(CustomerRepository customerRepo, AccountRepository accountRepo) {
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        
        
        Customer c = customerRepo.findByEmail(identifier)
                .orElseGet(() -> {
                    Account acc = accountRepo.findByAccountNumber(identifier)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    return acc.getCustomer();
                });
        
        String springSecurityRole =c.getRole();

        return new User(c.getEmail(), c.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(springSecurityRole)));
    }
}