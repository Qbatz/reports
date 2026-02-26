package com.smartstay.reports.controller;

import com.smartstay.reports.service.CustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/tenants")
public class CustomersController {
    @Autowired
    private CustomersService customersService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllTenants(@PathVariable("hostelId") String hostelId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return customersService.getCustomers(hostelId, startDate, endDate);
    }

    @GetMapping("/details/{hostelId}")
    public ResponseEntity<?> getAllTenantsDetails(@PathVariable("hostelId") String hostelId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return customersService.getCustomersDetails(hostelId, startDate, endDate);
    }
}
