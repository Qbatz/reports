package com.smartstay.reports.responses.settlement;

import java.util.List;

public record AdditionalAdvance(int totalInvoice,
                                Double invoiceAmount,
                                Double balanceAmount,
                                List<AdditionalAdvanceItems> additionalAdvanceItems) {
}
