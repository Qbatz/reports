package com.smartstay.reports.dto.invoice;

public record AccountDetails(String accountNo,
                             String ifscCode,
                             String bankName,
                             String upiId,
                             String qrCode) {
}
