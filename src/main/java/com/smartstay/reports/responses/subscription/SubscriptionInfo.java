package com.smartstay.reports.responses.subscription;

public record SubscriptionInfo(
        String planName,
        String planType,
        String planCode,
        double rate,
        double amount,
        double discountAmount,
        double cgst,
        double sgst,
        double cgstAmount,
        double sgstAmount,
        double total
) {
}
