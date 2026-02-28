package com.smartstay.reports.responses.receipts;

public record ReceiptList(String receiptNo,
                          String paidDate,
                          String tenant,
                          String invoiceNumber,
                          String amount,
                          String paymentMode,
                          String collectedBy,
                          String paymentType) {
}
