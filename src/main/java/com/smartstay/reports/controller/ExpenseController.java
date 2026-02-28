package com.smartstay.reports.controller;

import com.smartstay.reports.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;
    @GetMapping("/details/{hostelId}")
    public ResponseEntity<?> getExpenseDetails(@PathVariable("hostelId") String hostelId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate) {
        return expenseService.getExpenseDetails(hostelId, startDate, endDate);
    }
    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getExpense(@PathVariable("hostelId") String hostelId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate) {
        return expenseService.getExpense(hostelId, startDate, endDate);
    }
}
