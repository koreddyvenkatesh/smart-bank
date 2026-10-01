package com.bank.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.config.JwtService;
import com.bank.dto.request.AuthRequestDto;
import com.bank.dto.request.CustomerCreateRequestDto;
import com.bank.dto.request.LoanApplyRequestDto;
import com.bank.dto.request.PasswordResetRequestDto;
import com.bank.dto.request.TransferRequestDto;
import com.bank.dto.response.AccountResponseDto;
import com.bank.dto.response.AdminCustomerResponseDto;
import com.bank.dto.response.AdminLoanResponseDto;
import com.bank.dto.response.ApiResponseDto;
import com.bank.dto.response.AuthResponseDto;
import com.bank.dto.response.CustomerResponseDto;
import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.entity.Loan;
import com.bank.entity.Otp;
import com.bank.entity.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.LoanRepository;
import com.bank.repository.OtpRepository;
import com.bank.repository.TransactionRepository;



@Service
public class BankService {

	private final CustomerRepository customerRepository;
	private final AccountRepository accountRepository;
	private final LoanRepository loanRepository;
	private final TransactionRepository transactionRepository;
	private final OtpRepository otpRepository;
	private final PasswordEncoder encoder;
	private final JavaMailSender javaMailSender;
	private final JwtService jwtService;
	private final Random random=new Random();

	
	public BankService(CustomerRepository customerRepository, AccountRepository accountRepository,
			LoanRepository loanRepository, TransactionRepository transactionRepository, OtpRepository otpRepository,
			PasswordEncoder passwordEncoder, JavaMailSender javaMailSender, JwtService jwtService) {
		super();
		this.customerRepository = customerRepository;
		this.accountRepository = accountRepository;
		this.loanRepository = loanRepository;
		this.transactionRepository = transactionRepository;
		this.otpRepository = otpRepository;
		this.encoder = passwordEncoder;
		this.javaMailSender = javaMailSender;
		this.jwtService = jwtService;
	}
	
	@Transactional(readOnly = true)
	public AuthResponseDto authenticateUser(AuthRequestDto req) {
	    Customer customer = customerRepository.findByEmail(req.getEmailOrAccountNumber())
	            .orElseGet(() -> {
	                Account acc = accountRepository.findByAccountNumber(req.getEmailOrAccountNumber())
	                        .orElseThrow(() -> new RuntimeException("User not found"));
	                return acc.getCustomer();
	            });

	    if (!customer.isActive()) {
	        throw new RuntimeException("Account is pending Admin approval.");
	    }

	    if (customer.isTempPasswordActive()) {
	        // CORRECTED: Use standard string comparison for the plaintext temporary password
	        if (!req.getPassword().equals(customer.getTempPassword())) {
	            throw new RuntimeException("Invalid temporary password.");
	        }
	        return new AuthResponseDto(null, "RESET_REQUIRED", customer.getRole());
	    } 
	    
	    if (!encoder.matches(req.getPassword(), customer.getPassword())) {
	        throw new RuntimeException("Invalid credentials.");
	    }

	    String token = jwtService.generateToken(customer.getEmail(), customer.getRole());
	    return new AuthResponseDto(token, "Login successful", customer.getRole());
	}
	
	// Add this import if you don't have it: 
	// import org.springframework.transaction.annotation.Transactional;

	@Transactional
	public ApiResponseDto resetPassword(PasswordResetRequestDto req) {
	    // 1. Find the customer
	    Customer customer = customerRepository.findByEmail(req.getEmailOrAccountNumber())
	            .orElseGet(() -> {
	                Account acc = accountRepository.findByAccountNumber(req.getEmailOrAccountNumber())
	                        .orElseThrow(() -> new RuntimeException("User not found"));
	                return acc.getCustomer();
	            });

	    // 2. Verify the OTP
	    boolean isOtpValid = verifyOtp(customer.getEmail(), req.getOtpCode());
	    
	    if (!isOtpValid) {
	        throw new RuntimeException("Invalid or expired OTP.");
	    }

	    // 3. Hash the new password and save
	    customer.setPassword(encoder.encode(req.getNewPassword()));
	    
	    // Clear temp passwords
	    customer.setTempPassword(null);
	    customer.setTempPasswordActive(false);
	    
	    customerRepository.save(customer);

	    // 4. NOW consume/delete the OTP so it cannot be reused
	    Otp otpEntity = otpRepository.findByIdentifierAndOtpCode(customer.getEmail(), req.getOtpCode()).orElse(null);
	    if (otpEntity != null) {
	        otpRepository.delete(otpEntity);
	    }

	    return new ApiResponseDto("Password reset successfully. You can now log in.", true);
	}
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerProfile(String identifier) {
        Customer customer = customerRepository.findByEmail(identifier)
                .orElseGet(() -> {
                    Account acc = accountRepository.findByAccountNumber(identifier)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));
                    return acc.getCustomer();
                });

        CustomerResponseDto response = new CustomerResponseDto();
        response.setFullName(customer.getFullName());
        response.setEmail(customer.getEmail());
        response.setMobile(customer.getMobile());
        response.setKycStatus(customer.getKycStatus());

        List<AccountResponseDto> accountDtos = customer.getAccounts().stream()
            .map(acc -> {
                AccountResponseDto accDto = new AccountResponseDto();
                accDto.setAccountNumber(acc.getAccountNumber());
                accDto.setAccountType(acc.getAccountType());
                accDto.setBalance(acc.getBalance());
                accDto.setStatus(acc.getStatus());
                return accDto;
            })
            .collect(Collectors.toList());

        response.setAccounts(accountDtos);
        return response;
    }

	public ApiResponseDto intiateAccountCreation(CustomerCreateRequestDto customerCreateRequestDto) {
		if(customerRepository.existsByEmail(customerCreateRequestDto.getEmail())) {
			return new ApiResponseDto("Email already exits", false);
			
		}
		if(customerRepository.existsByMobile(customerCreateRequestDto.getMobile())) {
			return new ApiResponseDto("Mobile number already exists ", false);
		}
		Customer customer=new Customer();
		customer.setFullName(customerCreateRequestDto.getFullName());
		customer.setEmail(customerCreateRequestDto.getEmail());
		customer.setMobile(customerCreateRequestDto.getMobile());
		customer.setKycStatus("PENDING");
		customer.setRole("CUSTOMER");
		
		Account account=new Account();
		account.setAccountType(customerCreateRequestDto.getInitialAccountType());
		account.setStatus("PENDING");
		account.setBalance(0.0);
		account.setCustomer(customer);
		
		customer.getAccounts().add(account);
		customerRepository.save(customer);
		
		return new ApiResponseDto("Customer Kyc and intial account submitted to admin", true);
	
	}
	
	@Transactional
	public ApiResponseDto applyForLoan(LoanApplyRequestDto loanApplyRequestDto,String targetAccountNumber) {
		
		Account acc=accountRepository.findByAccountNumber(targetAccountNumber).orElseThrow(()->new RuntimeException("Account not found"));
		
		if(!"APPROVED".equals(acc.getStatus())) {
			return new ApiResponseDto("cannot apply for loan as account not approved", false);
		}
		
		Loan loan=new Loan();
		loan.setAccountNumber(targetAccountNumber);
		loan.setLoanType(loanApplyRequestDto.getLoanType());
		loan.setAmount(loanApplyRequestDto.getAmount());
		loan.setTenureMonths(loanApplyRequestDto.getTenureMonths());
		loan.setInterestRate(loanApplyRequestDto.getLoanType().equalsIgnoreCase("HOME") ? 8.5 : 12);
		loan.setStatus("PENDING");
		loan.setAppliedDate(LocalDate.now());
		
		loanRepository.save(loan);
		
		return new ApiResponseDto("Loan application submitted for Admin approval", true);
		
	}
	
	public ApiResponseDto approveCustomerKyc(Long customerId) {
		Customer customer=customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
		
		if(!"PENDING".equals(customer.getKycStatus())) {
			return new ApiResponseDto("kyc is already processed", false);
		}
		customer.setKycStatus("APPROVED");
		customer.setActive(true);
		
		String tempPass="TMP"+(1000+random.nextInt(9000));
		customer.setTempPassword(tempPass);
		customer.setPassword(encoder.encode(tempPass));
		customer.setTempPasswordActive(true);
		
		for(Account acc:customer.getAccounts()) {
			if("PENDING".equals(acc.getStatus())) {
				acc.setStatus("APPROVED");
				acc.setAccountNumber("10" + (1000000000L + (long) (random.nextDouble() * 9000000000L)));
			}
		}
		customerRepository.save(customer);
		sendEmail(customer.getEmail(), "Smart Bank Account Approved", 
	            "Your KYC is approved.\nTemporary Password: " + tempPass + "\nPlease login to view your Account Number(s).");
		
		return new ApiResponseDto("Customer KYC and accounts approved.", true);
	
	}
	
	private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
	
	public ApiResponseDto reviewCustomerKyc(Long customerId, String action, String reason) {
	    Customer customer = customerRepository.findById(customerId)
	            .orElseThrow(() -> new RuntimeException("Customer not found"));
	    
	    if (!"PENDING".equals(customer.getKycStatus())) {
	        return new ApiResponseDto("KYC is already processed", false);
	    }

	    if ("APPROVE".equalsIgnoreCase(action)) {
	        customer.setKycStatus("APPROVED");
	        customer.setActive(true);
	        
	        String tempPass = "TMP" + (1000 + random.nextInt(9000));
	        customer.setTempPassword(tempPass);
	        customer.setPassword(encoder.encode(tempPass));
	        customer.setTempPasswordActive(true);
	        
	        for (Account acc : customer.getAccounts()) {
	            if ("PENDING".equals(acc.getStatus())) {
	                acc.setStatus("APPROVED");
	                acc.setAccountNumber("10" + (1000000000L + (long) (random.nextDouble() * 9000000000L)));
	            }
	        }
	        
	        customerRepository.save(customer);
	        sendEmail(customer.getEmail(), "Smart Bank Account Approved", 
	                "Your KYC is approved.\nTemporary Password: " + tempPass + "\nPlease login to view your Account Number(s).");
	        
	        return new ApiResponseDto("Customer KYC and accounts approved.", true);

	    } else if ("REJECT".equalsIgnoreCase(action)) {
	        customer.setKycStatus("REJECTED");
	        customer.setActive(false);
	    
	        for (Account acc : customer.getAccounts()) {
	            if ("PENDING".equals(acc.getStatus())) {
	                acc.setStatus("REJECTED");
	            }
	        }
	        
	        customerRepository.save(customer);
	        
	        String emailMessage = "Your KYC application has been rejected.";
	        if (reason != null && !reason.trim().isEmpty()) {
	            emailMessage += "\nReason: " + reason;
	        }
	        emailMessage += "\nPlease contact support for more details.";
	        
	        sendEmail(customer.getEmail(), "Smart Bank Account KYC Rejected", emailMessage);
	        
	        return new ApiResponseDto("Customer KYC rejected.", true);
	        
	    } else {
	        return new ApiResponseDto("Invalid review action. Use APPROVE or REJECT.", false);
	    }
	}
	
	
	@Transactional
	public ApiResponseDto approveLoan(Long loanId) {
		
		Loan loan = loanRepository.findById(loanId).orElseThrow(()-> new RuntimeException("Loan not found"));
		
		if (!"PENDING".equals(loan.getStatus())) {
            return new ApiResponseDto("Loan is already processed", false);
        }
		
		Account account=accountRepository.findByAccountNumber(loan.getAccountNumber()).orElseThrow(()-> new RuntimeException("target account not found"));
		
		account.setBalance(account.getBalance()+loan.getAmount());
		loan.setStatus("APPROVED");
		
		accountRepository.save(account);
		loanRepository.save(loan);
		
		return new ApiResponseDto("loan is approved and send to  "+account.getAccountNumber(),true);
	}
	
	@Transactional
	public ApiResponseDto transferFunds(String fromAccount, TransferRequestDto transferRequestDto) {
		
		if(fromAccount.equals(transferRequestDto.getToAccount())) {
			return new ApiResponseDto("transfer to self not possible", false);
		}
		
		Account sender=accountRepository.findByAccountNumber(fromAccount).orElseThrow(()-> new RuntimeException("sender account not found"));
		Account receiver=accountRepository.findByAccountNumber(transferRequestDto.getToAccount()).orElseThrow(()-> new RuntimeException("receiver account not found"));
		
		if(!"APPROVED".equals(receiver.getStatus())) {
			return new ApiResponseDto("receivers account is not active", false);
		}
		
		if(sender.getBalance()<transferRequestDto.getAmount()) {
			return new ApiResponseDto("Insufficient balance!!!", false);
		}
		
		sender.setBalance(sender.getBalance()-transferRequestDto.getAmount());
		receiver.setBalance(receiver.getBalance()+transferRequestDto.getAmount());
		
		Transaction tx=new Transaction();
		tx.setFromAccount(fromAccount);
		tx.setToAccount(transferRequestDto.getToAccount());
		tx.setAmount(transferRequestDto.getAmount());
		tx.setType("TRANSFER");
		tx.setTransactionDate(LocalDateTime.now());
		
		accountRepository.save(sender);
		accountRepository.save(receiver);
		transactionRepository.save(tx);
		
		return new ApiResponseDto("Transfer Successful", true);
		
	}
	
	@Transactional
	public ApiResponseDto generateAndSendOtp(String mail) {
		
		Customer customer=customerRepository.findByEmail(mail).orElseThrow(()->new RuntimeException("not existing customer found"));
		String otpCode=String.format("%06d",random.nextInt(999999));
		
		Otp otp=new Otp();
		otp.setIdentifier(customer.getEmail());
		otp.setOtpCode(otpCode);
		otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
		
		otpRepository.save(otp);
		
		sendEmail(customer.getEmail(), "Smart Bank ", 
	            "Your otp for reset: " + otpCode + "\nPlease enter and reset your password");
		
		return new ApiResponseDto("otp sent successfully!!", true);
	}
	
	@Transactional
    public boolean verifyOtp(String email, String otpCode) {
        
        Otp otpEntity = otpRepository.findByIdentifierAndOtpCode(email, otpCode)
                .orElse(null);

        
        if (otpEntity == null || otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            if (otpEntity != null) otpRepository.delete(otpEntity); 
            return false;
        }

       
        return true;
    }
	
	
	public List<AdminCustomerResponseDto> getPendingCustomers() {

	    List<Customer> customers =
	            customerRepository.findByKycStatus("PENDING");

	    return customers.stream().map(customer -> {

	        AdminCustomerResponseDto dto = new AdminCustomerResponseDto();

	        dto.setId(customer.getId());
	        dto.setFullName(customer.getFullName());
	        dto.setEmail(customer.getEmail());
	        dto.setMobile(customer.getMobile());
	        dto.setKycStatus(customer.getKycStatus());
	        dto.setActive(customer.isActive());

	        List<AccountResponseDto> accounts =
	                customer.getAccounts().stream().map(account -> {

	                    AccountResponseDto accountDto =
	                            new AccountResponseDto();

	                    accountDto.setAccountNumber(account.getAccountNumber());
	                    accountDto.setAccountType(account.getAccountType());
	                    accountDto.setBalance(account.getBalance());
	                    accountDto.setStatus(account.getStatus());

	                    return accountDto;

	                }).toList();

	        dto.setAccounts(accounts);

	        return dto;

	    }).toList();
	}

	public List<AdminLoanResponseDto> getPendingLoans() {

	    List<Loan> loans =
	            loanRepository.findByStatus("PENDING");

	    return loans.stream().map(loan -> {

	        AdminLoanResponseDto dto =
	                new AdminLoanResponseDto();

	        dto.setId(loan.getId());
	        dto.setAccountNumber(loan.getAccountNumber());
	        dto.setLoanType(loan.getLoanType());
	        dto.setAmount(loan.getAmount());
	        dto.setTenureMonths(loan.getTenureMonths());
	        dto.setInterestRate(loan.getInterestRate());
	        dto.setStatus(loan.getStatus());
	        dto.setAppliedDate(loan.getAppliedDate());

	        return dto;

	    }).toList();
	}
	
	
	
	
}
