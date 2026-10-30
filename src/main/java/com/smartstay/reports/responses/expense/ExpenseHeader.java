package com.smartstay.reports.responses.expense;

public record ExpenseHeader(String totalExpenses,
                            String totalExpenseAmount,
                            String totalPaidAmount,
                            String totalUnpaidAmount,
                            String totalPartiallyPaidAmount,
                            String startDate,
                            String endDate) {
}
