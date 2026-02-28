package com.smartstay.reports.controller;

import com.smartstay.reports.service.TransactionV1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/report/{hostelId}")
    public ResponseEntity<?> getReceiptsReports(@PathVariable("hostelId") String hostelId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate) {
        return transctionService.getReceiptReports(hostelId, startDate, endDate);
    }

    @GetMapping("/report/details/{hostelId}")
    public ResponseEntity<?> getReceiptsReportsDetails(@PathVariable("hostelId") String hostelId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate) {
        return transctionService.getReceiptReportsDetails(hostelId, startDate, endDate);
    }
}
