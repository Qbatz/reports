package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.responses.expense.ExpensesList;
import com.smartstay.reports.responses.expense.ExpensesResponse;
import com.smartstay.reports.utils.BankingUtils;
import com.smartstay.reports.utils.NameUtils;
import com.smartstay.reports.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class ExpensesMapper implements Function<ExpensesV1, ExpensesList> {

    List<ExpenseCategory> listCategories = null;
    List<ExpenseSubCategory> listSubCategories = null;
    List<BankingV1> listBanks = null;
    List<VendorV1> listVendors = null;

    public ExpensesMapper(List<ExpenseCategory> listCategories, List<ExpenseSubCategory> listSubCategories, List<BankingV1> listBanks, List<VendorV1> vendors) {
        this.listCategories = listCategories;
        this.listSubCategories = listSubCategories;
        this.listBanks = listBanks;
        this.listVendors = vendors;
    }

    @Override
    public ExpensesList apply(ExpensesV1 expensesV1) {
        String categoryName = "NA";
        String subCategoryName = null;
        String description = "NA";
        String amount = "NA";
        String unitCount = "NA";
        String assignedAssets = "NA";
        String vendorName = null;
        String debitedFrom = null;
        String balanceAmount = null;
        double totalAmount = 0.0;
        double bAmount = 0.0;
        String paidAmount = null;

        if (expensesV1.getDescription() != null) {
            description = expensesV1.getDescription();
        }

        if (expensesV1.getTransactionAmount() != null) {
            amount = String.valueOf(expensesV1.getTransactionAmount());
            totalAmount = expensesV1.getTransactionAmount();
        }
        if (expensesV1.getUnitCount() != null) {
            unitCount = String.valueOf(expensesV1.getUnitCount());
        }
        if (expensesV1.getBalanceAmount() != null) {
            bAmount = expensesV1.getBalanceAmount();
            balanceAmount = String.valueOf(Utils.roundOffWithTwoDigit(expensesV1.getBalanceAmount()));
        }
        paidAmount = String.valueOf((totalAmount - bAmount));

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
                        .filter(i -> i.getSubCategoryId().equals(expensesV1.getSubCategoryId()))
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

        if (expensesV1.getIsVendorExpense() != null && expensesV1.getIsVendorExpense()) {
            if (listVendors != null) {
                VendorV1 vendorV1 = listVendors
                        .stream()
                        .filter(i -> expensesV1.getVendorId().equals(i.getVendorId()))
                        .findFirst()
                        .orElse(null);
                if (vendorV1 != null) {
                    vendorName = NameUtils.getFullName(vendorV1.getFirstName(), vendorV1.getLastName());
                }
            }
        }



        return new ExpensesList(Utils.dateToString(expensesV1.getTransactionDate()),
                categoryName,
                subCategoryName,
                description,
                expensesV1.getTitle(),
                expensesV1.getPaymentStatus().name(),
                amount,
                String.valueOf(paidAmount),
                balanceAmount,
                unitCount,
                assignedAssets,
                vendorName,
                debitedFrom);
    }
}
