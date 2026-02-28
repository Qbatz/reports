package com.smartstay.reports.responses.expense;

public record ExpensesList(String date,
                           String category,
                           String subCategory,
                           String description,
                           String amount,
                           String unitCount,
                           String assignedAssets,
                           String vendors,
                           String debitedFrom) {
}
