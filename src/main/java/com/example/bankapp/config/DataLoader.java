package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final CustomerRepository customerRepository;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (customerRepository.count() == 0) {
                Customer c1 = Customer.builder()
                        .accountNumber("ACC001")
                        .fullName("Alice Smith")
                        .email("alice@example.com")
                        .balance(new BigDecimal("30000.00"))
                        .build();

                Customer c2 = Customer.builder()
                        .accountNumber("ACC002")
                        .fullName("Bob Jones")
                        .email("bob@example.com")
                        .balance(new BigDecimal("25000.00"))
                        .build();

                Customer c3 = Customer.builder()
                        .accountNumber("ACC003")
                        .fullName("Charlie Brown")
                        .email("charlie@example.com")
                        .balance(new BigDecimal("30000.00"))
                        .build();

                customerRepository.save(c1);
                customerRepository.save(c2);
                customerRepository.save(c3);
            }
        };
    }
}
