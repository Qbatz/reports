package com.smartstay.reports.controller;

import com.smartstay.reports.service.TransactionV1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/reports/receipts")
public class ReceiptsController {

    @Autowired
    private TransactionV1Service transctionService;

    @GetMapping("/{hostelId}/{transactionId}")
    public ResponseEntity<?> getReceiptDownload(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String transactionId) {
        return transctionService.getReceiptPDF(hostelId, transactionId);
    }

    @GetMapping("/details/{hostelId}/{transactionId}")
    public ResponseEntity<?> getReceiptdetails(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String transactionId) {
        return transctionService.getReceiptDetails(hostelId, transactionId);
    }
}
