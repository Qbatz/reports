package com.smartstay.reports.controller;

import com.smartstay.reports.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/reports/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceReports(@PathVariable("invoiceId") String invoiceId, @PathVariable("hostelId") String hostelId) {
        return invoiceService.getInvoiceReport(hostelId, invoiceId);
    }

    @GetMapping("/details/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceReportsDetails(@PathVariable("invoiceId") String invoiceId, @PathVariable("hostelId") String hostelId) {
        return invoiceService.getInvoiceDetails(hostelId, invoiceId);
    }
}
