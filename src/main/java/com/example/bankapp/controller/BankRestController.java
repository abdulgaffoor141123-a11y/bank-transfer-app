package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BankRestController {

    @Autowired
    private BankService bankService;

    @PostMapping("/transfers")
    public ResponseEntity<String> transferFunds(@RequestBody TransferRequest request) {
        try {
            bankService.transfer(request);
            return ResponseEntity.ok("Transfer completed successfully via API!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Transfer failed: " + e.getMessage());
        }
    }
}
