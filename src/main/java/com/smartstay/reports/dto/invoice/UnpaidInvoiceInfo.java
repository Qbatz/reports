package com.smartstay.reports.dto.invoice;

import java.util.List;

public record UnpaidInvoiceInfo(int noOfUnpaidInvoices,
                                Double unpaidInvoiceTotalAmount,
                                List<UnpaidInvoiceItem> unpaidInvoiceItems) {
}
