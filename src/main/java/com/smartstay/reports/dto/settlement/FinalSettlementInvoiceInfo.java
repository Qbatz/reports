package com.smartstay.reports.dto.settlement;

public record FinalSettlementInvoiceInfo(String invoiceNo,
                                         String invoiceDate,
                                         String dueDate,
                                         String joiningDate,
                                         String rentalPeriod,
                                         Double payableRent,
                                         Double subTotal,
                                         Double deductionAmount,
                                         Double unpaidInvoiceAmount,
                                         Double electricityAmount,
                                         Double discountAmount,
                                         Double finalAmount,
                                         Double refundableAmount,
                                         Double nonRefundable,
                                         boolean isNewPattern,
                                         String status) {
}
