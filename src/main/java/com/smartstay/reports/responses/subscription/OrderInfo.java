package com.smartstay.reports.responses.subscription;

public record OrderInfo(
        String invoiceNumber,
        String invoiceDate,
        String terms,
        String dueDate,
        String referenceNumber
) {
}
