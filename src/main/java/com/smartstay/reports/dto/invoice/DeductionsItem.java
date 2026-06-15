package com.smartstay.reports.dto.invoice;

public record DeductionsItem(String item,
                             Double paidAmount,
                             Double amount,
                             Double pendingAmount) {
}
