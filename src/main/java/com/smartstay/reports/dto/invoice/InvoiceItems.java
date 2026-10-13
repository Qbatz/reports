package com.smartstay.reports.dto.invoice;

public record InvoiceItems(String invoiceNo,
                           String description,
                           Double amount) {
}