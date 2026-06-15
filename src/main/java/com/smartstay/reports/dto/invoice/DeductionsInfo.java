package com.smartstay.reports.dto.invoice;

import java.util.List;

public record DeductionsInfo(Double totalDeductionsAmount,
                             Double paidAmount,
                             Double pendingAmount,
                             List<DeductionsItem> listDeductions) {
}
