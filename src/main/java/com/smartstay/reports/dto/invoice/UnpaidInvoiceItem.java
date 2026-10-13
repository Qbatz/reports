package com.smartstay.reports.dto.invoice;

public record UnpaidInvoiceItem(String invoiceNumber,
                                Double invoiceAmount,
                                Double paidAmount,
                                Double pendingAmount) {
}
