package com.smartstay.reports.responses.receipts;

public record ReceiptInfo(String receiptNumber,
                          String date,
                          String time,
                          String paymentMode,
                          String transactionId) {
}
