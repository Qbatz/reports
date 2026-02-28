package com.smartstay.reports.service;

import com.smartstay.reports.dao.ExpensesV1;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
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

import java.util.Date;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private PDFServices pdfServices;

    public ResponseEntity<?> getExpenseDetails(String hostelId, String startDate, String endDate) {
        ExpensesResponse expenses = getExpensesResponse(hostelId, startDate, endDate);

        Context context = new Context();
        context.setVariable("expenses", expenses);

        String invoiceUrl = pdfServices.generateExpensesPdf("expenses", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getExpense(String hostelId, String startDate, String endDate) {
        ExpensesResponse expensesResponse = getExpensesResponse(hostelId, startDate, endDate);
        return new ResponseEntity<>(expensesResponse, HttpStatus.OK);
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
                String.valueOf(sDate),
                String.valueOf(eDate));

        List<ExpensesList> list = listExpenses
                .stream()
                .map(i -> new ExpensesMapper().apply(i))
                .toList();

        return new ExpensesResponse(hostelInformation,
                footerInfo,
                expenseHeader,
                list);
    }
}
