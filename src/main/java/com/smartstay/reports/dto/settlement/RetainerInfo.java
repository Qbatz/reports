package com.smartstay.reports.dto.settlement;

import java.util.List;

public record RetainerInfo(String appliedAmount,
                           String totalInvoiceAmount,
                           List<RetainerItemsList> retainerItemsList) {
}
