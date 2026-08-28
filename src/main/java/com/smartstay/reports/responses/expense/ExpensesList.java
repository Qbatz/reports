package com.smartstay.reports.responses.expense;

public record ExpensesList(String date,
                           String category,
                           String subCategory,
                           String description,
                           String expenseTitle,
                           String status,
                           String totalAmount,
                           String paidAmount,
                           String balance,
                           String unitCount,
                           String assignedAssets,
                           String vendors,
                           String debitedFrom) {
}
