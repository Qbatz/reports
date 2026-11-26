package com.smartstay.reports.payloads.table;

public record TableColumnPayload(int order,
                                 String fieldName,
                                 boolean isSelected) {
}
