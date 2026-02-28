package com.smartstay.reports.responses.expense;

public record ExpenseHeader(String totalExpenses,
                            String totalExpenseAmount,
                            String startDate,
                            String endDate) {
}
