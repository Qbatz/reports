package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.BankingV1;
import com.smartstay.reports.dao.ExpenseCategory;
import com.smartstay.reports.dao.ExpenseSubCategory;
import com.smartstay.reports.dao.ExpensesV1;
import com.smartstay.reports.responses.expense.ExpensesList;
import com.smartstay.reports.responses.expense.ExpensesResponse;
import com.smartstay.reports.utils.BankingUtils;
import com.smartstay.reports.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class ExpensesMapper implements Function<ExpensesV1, ExpensesList> {

    List<ExpenseCategory> listCategories = null;
    List<ExpenseSubCategory> listSubCategories = null;
    List<BankingV1> listBanks = null;

    public ExpensesMapper(List<ExpenseCategory> listCategories, List<ExpenseSubCategory> listSubCategories, List<BankingV1> listBanks) {
        this.listCategories = listCategories;
        this.listSubCategories = listSubCategories;
        this.listBanks = listBanks;
    }

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

        if (expensesV1.getDescription() != null) {
            description = expensesV1.getDescription();
        }

        if (expensesV1.getTotalPrice() != null) {
            amount = String.valueOf(expensesV1.getTransactionAmount());
        }
        if (expensesV1.getUnitCount() != null) {
            unitCount = String.valueOf(expensesV1.getUnitCount());
        }

        if (listBanks != null) {
            BankingV1 bankingV1 = listBanks
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(expensesV1.getBankId()))
                    .findFirst()
                    .orElse(null);

            if (bankingV1 != null) {
                debitedFrom = BankingUtils.getPaymentModeWithHolder(bankingV1);
            }
        }

        if (expensesV1.getSubCategoryId() != null) {
            if (listSubCategories != null) {
                ExpenseSubCategory subCategory = listSubCategories
                        .stream()
                        .filter(i -> i.getSubCategoryId().equals(expensesV1.getCategoryId()))
                        .findFirst()
                        .orElse(null);

                if (subCategory != null) {
                    subCategoryName = subCategory.getSubCategoryName();
                }
            }
        }

        if (listCategories != null) {
            ExpenseCategory expenseCategory = listCategories
                    .stream()
                    .filter(i -> i.getCategoryId().equals(expensesV1.getCategoryId()))
                    .findFirst()
                    .orElse(null);
            if (expenseCategory != null) {
                categoryName = expenseCategory.getCategoryName();
            }
        }



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
