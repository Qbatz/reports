package com.smartstay.reports.dto.invoice;

public record RedeemedInfo(String invoiceId,
                           String invoiceNumber,
                           String date,
                           String invoiceType,
                           String redeemedDate,
                           Double redeemedAmount,
                           Double invoiceAmount) {
}
