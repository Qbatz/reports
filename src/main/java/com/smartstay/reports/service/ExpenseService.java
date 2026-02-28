package com.smartstay.reports.service;

import com.smartstay.reports.dao.BankingV1;
import com.smartstay.reports.dao.ExpenseCategory;
import com.smartstay.reports.dao.ExpenseSubCategory;
import com.smartstay.reports.dao.ExpensesV1;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
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

    public ResponseEntity<?> getExpenseDetails(String hostelId, String startDate, String endDate) {
        ExpensesResponse expenses = getExpensesResponse(hostelId, startDate, endDate);


        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    public ResponseEntity<?> getExpense(String hostelId, String startDate, String endDate) {
        ExpensesResponse expenses = getExpensesResponse(hostelId, startDate, endDate);

        Context context = new Context();
        context.setVariable("expenses", expenses);

        String invoiceUrl = pdfServices.generateExpensesPdf("expenses", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    private ExpensesResponse getExpensesResponse(String hostelId, String startDate, String endDate) {
        Date sDate = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        List<ExpensesV1> listExpenses = expensesRepository.getAllExpenses(hostelId, sDate, eDate);
        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);
        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));
        ExpenseHeader expenseHeader = null;

        double totalExpenseAmount = listExpenses
                .stream()
                .mapToDouble(ExpensesV1::getTotalPrice)
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

        List<ExpensesList> list = listExpenses
                .stream()
                .map(i -> new ExpensesMapper(listCategories, listSubcatgories, banks).apply(i))
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
