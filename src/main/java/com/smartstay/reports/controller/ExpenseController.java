package com.smartstay.reports.controller;

import com.smartstay.reports.service.ExpenseService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/expense")
@CrossOrigin("*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
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
