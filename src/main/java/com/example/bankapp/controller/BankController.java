package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankService bankService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "index";
    }

    @ResponseBody 
    @GetMapping("/api/transfers/history/{accountNumber}")
    public ResponseEntity<List<BankTransaction>> getAccountHistory(@PathVariable String accountNumber) {
        List<BankTransaction> history = bankService.getAccountHistory(accountNumber);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/transfer-form")
    public String handleTransfer(@RequestParam("senderAccountNumber") String senderAccountNumber,
                                 @RequestParam("receiverAccountNumber") String receiverAccountNumber,
                                 @RequestParam("amount") double amount,
                                 @RequestParam(value = "simulateFailure", required = false, defaultValue = "false") boolean simulateFailure,
                                 Model model) {
        try {
            TransferRequest request = new TransferRequest();
            request.setFromAccount(senderAccountNumber);
            request.setToAccount(receiverAccountNumber);
            request.setAmount(BigDecimal.valueOf(amount));
            request.setSimulateFailure(simulateFailure);

            bankService.transfer(request);
            model.addAttribute("successMessage", "Transfer completed successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Transfer failed: " + e.getMessage());
        }

        model.addAttribute("customers", customerRepository.findAll());
        return "index";
    }
}