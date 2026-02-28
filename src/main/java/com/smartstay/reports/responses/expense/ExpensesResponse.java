package com.smartstay.reports.responses.expense;

import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HeaderInfo;
import com.smartstay.reports.dto.customer.HostelInformation;

import java.util.List;

public record ExpensesResponse(HostelInformation hostelInformation,
                               FooterInfo footerInfo,
                               ExpenseHeader expenseHeader,
                               List<ExpensesList> listExpenses) {
}
