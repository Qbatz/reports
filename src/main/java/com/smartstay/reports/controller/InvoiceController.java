package com.smartstay.reports.controller;

import com.smartstay.reports.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/pdf/details/{hostelId}")
    public ResponseEntity<?> getInvoiceReportDetails(@PathVariable("hostelId") String hostelId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return invoiceService.getInvoiceReportDetailDetails(hostelId, startDate, endDate);
    }

    @GetMapping("/pdf/{hostelId}")
    public ResponseEntity<?> getInvoiceReport(@PathVariable("hostelId") String hostelId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return invoiceService.getInvoiceReport(hostelId, startDate, endDate);
    }
}
