package com.smartstay.reports.responses.receipts;

public record ReceiptHeader(
        String totalAmount,
        String receivedAmount,
                            String returnedAmount,
                            String totalReceipts,
                            String startDate,
                            String endDate) {
}
