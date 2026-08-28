package com.smartstay.reports.controller;

import com.smartstay.reports.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;
    @GetMapping("/details/{hostelId}")
    public ResponseEntity<?> getExpenseDetails(@PathVariable("hostelId") String hostelId,
                                               @RequestParam(value = "startDate", required = false) String startDate,
                                               @RequestParam(value = "endDate", required = false) String endDate,
                                               @RequestParam(value = "categoryId", required = false) List<Long> categoryId,
                                               @RequestParam(value = "subCategoryId", required = false) List<Long> subCategoryId,
                                               @RequestParam(value = "paymentMode", required = false) List<String> paymentMode,
                                               @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
                                               @RequestParam(value = "paidTo", required = false) List<Integer> paidTo,
                                               @RequestParam(value = "createdBy", required = false) List<String> createdBy) {
        return expenseService.getExpenseDetails(hostelId, startDate, endDate, categoryId, subCategoryId, paymentMode, paymentStatus, paidTo, createdBy);
    }
    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getExpense(@PathVariable("hostelId") String hostelId,
                                        @RequestParam(value = "startDate", required = false) String startDate,
                                        @RequestParam(value = "endDate", required = false) String endDate,
                                        @RequestParam(value = "categoryId", required = false) List<Long> categoryId,
                                        @RequestParam(value = "subCategoryId", required = false) List<Long> subCategoryId,
                                        @RequestParam(value = "paymentMode", required = false) List<String> paymentMode,
                                        @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
                                        @RequestParam(value = "paidTo", required = false) List<Integer> paidTo,
                                        @RequestParam(value = "createdBy", required = false) List<String> createdBy) {
        return expenseService.getExpense(hostelId, startDate, endDate, categoryId, subCategoryId, paymentMode, paymentStatus, paidTo, createdBy);
    }
}
