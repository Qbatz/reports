package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.ExpensesV1;
import com.smartstay.reports.responses.expense.ExpensesList;
import com.smartstay.reports.responses.expense.ExpensesResponse;
import com.smartstay.reports.utils.Utils;

import java.util.function.Function;

public class ExpensesMapper implements Function<ExpensesV1, ExpensesList> {
    @Override
    public ExpensesList apply(ExpensesV1 expensesV1) {
        String categoryName = "NA";
        String subCategoryName = "NA";
        String description = "NA";
        String amount = "NA";
        String unitCount = "NA";
        String assignedAssets = "NA";
        String vendorName = "NA";
        String debitedFrom = "NA";


        return new ExpensesList(Utils.dateToString(expensesV1.getTransactionDate()),
                categoryName,
                subCategoryName,
                description,
                amount,
                unitCount,
                assignedAssets,
                vendorName,
                debitedFrom);
    }
}
