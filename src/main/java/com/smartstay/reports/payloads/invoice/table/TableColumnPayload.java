package com.smartstay.reports.payloads.invoice.table;

public record TableColumnPayload(int order,
                                 String fieldName,
                                 boolean isSelected) {
}
