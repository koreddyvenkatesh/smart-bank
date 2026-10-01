package com.bank.config;

import com.bank.entity.Customer;
import com.bank.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (customerRepository.findByEmail("admin").isEmpty()) {
            Customer admin = new Customer();
            admin.setFullName("System Administrator");
            admin.setEmail("admin");
            admin.setMobile("0000000000");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setTempPasswordActive(false);

            customerRepository.save(admin);
            System.out.println("✅ Default Admin user successfully seeded into the database.");
        }
    }
}