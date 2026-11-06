package com.example.bankapp.service;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.*;
import com.example.bankapp.model.*;
import com.example.bankapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Self-injection with @Lazy solves the Spring proxy bypass for internal REQUIRES_NEW calls
    @Autowired
    @Lazy
    private BankService self;

    // RESTORED: Full production transaction protection and rollback boundary
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(TransferRequest request) {
        String from = request.getFromAccount();
        String to = request.getToAccount();
        BigDecimal amount = request.getAmount();

        try {
            if (from.equals(to)) {
                throw new SameAccountException("Sender and receiver accounts must be different");
            }
            if (!customerRepository.countAccounts(from)) {
                throw new AccountNotFoundException("Sender account " + from + " not found");
            }
            if (!customerRepository.countAccounts(to)) {
                throw new AccountNotFoundException("Receiver account " + to + " not found");
            }

            int rowsUpdated = customerRepository.debit(from, amount);
            if (rowsUpdated == 0) {
                throw new InsufficientBalanceException("Insufficient balance in account " + from);
            }

            if (Boolean.TRUE.equals(request.getSimulateFailure())) {
                throw new RuntimeException("Simulated system failure after the withdrawal");
            }

            customerRepository.credit(to, amount);

            // Save SUCCESS transaction
            BankTransaction successTx = BankTransaction.builder()
                    .fromAccount(from)
                    .toAccount(to)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .description("Transfer completed successfully")
                    .createdAt(LocalDateTime.now())
                    .build();

            return transactionRepository.save(successTx);

        } catch (Exception e) {
            self.saveFailedTransaction(from, to, amount, e.getMessage());
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransaction(String from, String to, BigDecimal amount, String reason) {
        try {
            BankTransaction failedTx = BankTransaction.builder()
                    .fromAccount(from)
                    .toAccount(to)
                    .amount(amount)
                    .status(TransactionStatus.FAILED)
                    .description("Failed: " + reason)
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(failedTx);
        } catch (Exception ex) {
            System.err.println("CRITICAL: Failed to save audit log for failure: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Customer createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber())) {
            throw new RuntimeException("Account number already exists");
        }
        if (customerRepository.countEmails(customer.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerRepository.searchByName(keyword.trim());
        }
        return customerRepository.findAllOrdered();
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public long getSuccessCount() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    public long getFailedCount() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }
}