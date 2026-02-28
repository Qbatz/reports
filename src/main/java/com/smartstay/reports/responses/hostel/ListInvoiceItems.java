package com.smartstay.reports.responses.hostel;

public record ListInvoiceItems(String invoiceNumber,
                               String name,
                               String invoiceType,
                               String invoiceDate,
                               String dueDate,
                               String invoiceAmount,
                               String dueAmount,
                               String status) {
}
