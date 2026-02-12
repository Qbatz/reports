package com.smartstay.reports.controller;

import com.smartstay.reports.service.TransactionV1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/reports/receipts")
public class ReportsController {

    @Autowired
    private TransactionV1Service transctionService;

    @RequestMapping("/{hostelId}/{transactionId}")
    public ResponseEntity<?> getReceiptDownload(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String transactionId) {
        return transctionService.getReceiptPDF(hostelId, transactionId);
    }
}
