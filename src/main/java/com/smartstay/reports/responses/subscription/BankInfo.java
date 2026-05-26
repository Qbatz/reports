package com.smartstay.reports.responses.subscription;

public record BankInfo(
        String name,
        String accountNumber,
        String ifsc,
        String bankName,
        String branchName
) {
}
