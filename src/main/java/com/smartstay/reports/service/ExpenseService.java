package com.smartstay.reports.service;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.ennum.ExpensePaymentStatus;
import com.smartstay.reports.repositories.ExpenseCategoryRepository;
import com.smartstay.reports.repositories.ExpenseSubCategoryRepository;
import com.smartstay.reports.repositories.ExpensesRepository;
import com.smartstay.reports.responses.expense.ExpenseHeader;
import com.smartstay.reports.responses.expense.ExpensesList;
import com.smartstay.reports.responses.expense.ExpensesResponse;
import com.smartstay.reports.responses.invoice.InvoiceInfo;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.wrappers.ExpensesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ExpenseService {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private ExpenseSubCategoryRepository expenseSubCategoryRepository;
    @Autowired
    private PDFServices pdfServices;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private VendorService vendorService;

    public ResponseEntity<?> getExpenseDetails(String hostelId, String startDate, String endDate, List<Long> categoryId, List<Long> subCategoryId, List<String> paymentMode, List<String> paymentStatus, List<Integer> paidTo, List<String> createdBy) {
        ExpensesResponse expenses = getExpensesResponse(hostelId, startDate, endDate, categoryId, subCategoryId, paymentMode, paymentStatus, paidTo, createdBy);


        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    public ResponseEntity<?> getExpense(String hostelId, String startDate, String endDate, List<Long> categoryId, List<Long> subCategoryId, List<String> paymentMode, List<String> paymentStatus, List<Integer> paidTo, List<String> createdBy) {
        ExpensesResponse expenses = getExpensesResponse(hostelId, startDate, endDate, categoryId, subCategoryId, paymentMode, paymentStatus, paidTo, createdBy);

        Context context = new Context();
        context.setVariable("expenses", expenses);

        String invoiceUrl = pdfServices.generateExpensesPdf("expenses", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    private ExpensesResponse getExpensesResponse(String hostelId, String startDate, String endDate, List<Long> categoryId, List<Long> subCategoryId, List<String> paymentMode, List<String> paymentStatus, List<Integer> paidTo, List<String> createdBy) {
        Date sDate = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        List<ExpensePaymentStatus> pStatus = null;
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            pStatus = new ArrayList<>();
            List<ExpensePaymentStatus> finalPStatus = pStatus;
            paymentStatus.forEach(item -> {
                if (item.equalsIgnoreCase(ExpensePaymentStatus.Full.name())) {
                    finalPStatus.add(ExpensePaymentStatus.Full);
                }
                else if (item.equalsIgnoreCase(ExpensePaymentStatus.Partial.name())) {
                    finalPStatus.add(ExpensePaymentStatus.Partial);
                }
                else if (item.equalsIgnoreCase(ExpensePaymentStatus.Overdue.name())) {
                    finalPStatus.add(ExpensePaymentStatus.Overdue);
                }
                else if (item.equalsIgnoreCase(ExpensePaymentStatus.Pending.name())) {
                    finalPStatus.add(ExpensePaymentStatus.Pending);
                }
            });
        }
        if (pStatus != null && pStatus.isEmpty()) {
            pStatus = null;
        }
        List<Long> catId = null;
        List<Long> subCatId = null;
        List<String> pMode = null;
        List<Integer> pTo = null;
        List<String> cBy = null;

        if (categoryId != null && categoryId.isEmpty()) {
            catId = null;
        }
        else {
            catId = categoryId;
        }

        if (subCategoryId != null && subCategoryId.isEmpty()) {
            subCatId = null;
        }
        else {
            subCatId = subCategoryId;
        }

        if (paymentMode != null && paymentMode.isEmpty()) {
            pMode = null;
        }
        else {
            pMode = paymentMode;
        }
        if (paidTo != null && paidTo.isEmpty()) {
            pTo = null;
        }
        else {
            pTo = paidTo;
        }

        if (createdBy != null && createdBy.isEmpty()) {
            cBy = null;
        }
        else {
            cBy = createdBy;
        }

        List<ExpensesV1> listExpenses = expensesRepository.getAllExpenses(hostelId, sDate, eDate, catId, subCatId, pMode, pStatus, pTo, cBy);
        List<Integer> vendorExpenseIds = null;
        List<VendorV1> listVendors = null;
        if (listExpenses != null) {
            vendorExpenseIds = listExpenses
                    .stream()
                    .filter(i -> i.getIsVendorExpense() != null && i.getIsVendorExpense())
                    .map(ExpensesV1::getVendorId)
                    .toList();
        }
        if (vendorExpenseIds != null) {
            listVendors = vendorService.getVendorsByVendorIds(vendorExpenseIds);
        }

        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);
        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));
        ExpenseHeader expenseHeader = null;

        double totalExpenseAmount = listExpenses
                .stream()
                .mapToDouble(ExpensesV1::getTransactionAmount)
                .sum();

        expenseHeader = new ExpenseHeader(String.valueOf(listExpenses.size()),
                String.valueOf(totalExpenseAmount),
                Utils.dateToString(sDate),
                Utils.dateToString(eDate));

        List<String> bankIds = listExpenses
                .stream()
                .map(ExpensesV1::getBankId)
                .distinct()
                .toList();
        List<Long> categories = listExpenses
                .stream()
                .map(ExpensesV1::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> subCategories = listExpenses
                .stream()
                .map(ExpensesV1::getSubCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<ExpenseCategory> listCategories = getExpenseCategories(categories);
        List<ExpenseSubCategory> listSubcatgories;
        if (subCategories != null && !subCategories.isEmpty()) {
            listSubcatgories = getExpenseSubcategories(subCategories);
        } else {
            listSubcatgories = new ArrayList<>();
        }

        List<BankingV1> banks = bankingService.findByBankIds(bankIds);

        List<VendorV1> finalListVendors = listVendors;
        List<ExpensesList> list = listExpenses
                .stream()
                .map(i -> new ExpensesMapper(listCategories, listSubcatgories, banks, finalListVendors).apply(i))
                .toList();

        return new ExpensesResponse(hostelInformation,
                footerInfo,
                expenseHeader,
                list);
    }


    public List<ExpenseCategory> getExpenseCategories(List<Long> ids) {
        return expenseCategoryRepository.findAllById(ids);
    }

    public List<ExpenseSubCategory> getExpenseSubcategories(List<Long> ids) {
        return expenseSubCategoryRepository.findAllById(ids);
    }

}
