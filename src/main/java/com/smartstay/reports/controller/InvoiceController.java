package com.smartstay.reports.controller;

import com.smartstay.reports.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @GetMapping("/new/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceReportsSettlmentNew(@PathVariable("invoiceId") String invoiceId, @PathVariable("hostelId") String hostelId) {
        return invoiceService.getInvoiceReportNew(hostelId, invoiceId);
    }

    @GetMapping("/new/details/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceReportsDetailsSettlementNew(@PathVariable("invoiceId") String invoiceId, @PathVariable("hostelId") String hostelId) {
        return invoiceService.getNewSettlementDetails(hostelId, invoiceId);
    }
    @GetMapping("/new/retainer/details/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceReportsDetailsRetainers(@PathVariable("invoiceId") String invoiceId, @PathVariable("hostelId") String hostelId) {
        return invoiceService.getNewRetainerDetails(hostelId, invoiceId);
    }
    @GetMapping("/pdf/details/{hostelId}")
    public ResponseEntity<?> getInvoiceReportDetails(@PathVariable("hostelId") String hostelId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return invoiceService.getInvoiceReportDetailDetails(hostelId, startDate, endDate);
    }

    @GetMapping("/pdf/{hostelId}")
    public ResponseEntity<?> getInvoiceReport(@PathVariable("hostelId") String hostelId,
                                              @RequestParam(value = "search", required = false) String search,
                                              @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
                                              @RequestParam(value = "invoiceModes", required = false) List<String> invoiceModes,
                                              @RequestParam(value = "invoiceTypes", required = false) List<String> invoiceTypes,
                                              @RequestParam(value = "createdBy", required = false) List<String> createdBy,
                                              @RequestParam(value = "period", required = false) String period,
                                              @RequestParam(value = "minPaidAmount", required = false) Double minPaidAmount,
                                              @RequestParam(value = "maxPaidAmount", required = false) Double maxPaidAmount,
                                              @RequestParam(value = "minOutstandingAmount", required = false) Double minOutstandingAmount,
                                              @RequestParam(value = "maxOutstandingAmount", required = false) Double maxOutstandingAmount,
                                              @RequestParam("startDate") String startDate,
                                              @RequestParam("endDate") String endDate) {
        return invoiceService.getInvoiceReport(hostelId, search, paymentStatus, invoiceModes,
                invoiceTypes, createdBy, period, minPaidAmount, maxPaidAmount,
                minOutstandingAmount, maxOutstandingAmount, startDate, endDate);
    }
}
