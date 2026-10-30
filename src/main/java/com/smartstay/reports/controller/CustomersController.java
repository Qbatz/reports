package com.smartstay.reports.controller;

import com.smartstay.reports.service.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/tenants")
@CrossOrigin("*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class CustomersController {
    @Autowired
    private CustomersService customersService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllTenants(@PathVariable("hostelId") String hostelId,
                                           @RequestParam(value = "search", required = false) String search,
                                           @RequestParam(value = "status", required = false) List<String> status,
                                           @RequestParam(value = "room", required = false) List<Integer> room,
                                           @RequestParam(value = "floor", required = false) List<Integer> floor,
                                           @RequestParam(value = "sharingType", required = false) List<String> sharingType,
                                           @RequestParam("startDate") String startDate,
                                           @RequestParam("endDate") String endDate) {
        return customersService.getCustomers(hostelId, search, status, room, floor, sharingType, startDate, endDate);
    }

    @GetMapping("/details/{hostelId}")
    public ResponseEntity<?> getAllTenantsDetails(@PathVariable("hostelId") String hostelId,
                                                  @RequestParam(value = "search", required = false) String search,
                                                  @RequestParam(value = "status", required = false) List<String> status,
                                                  @RequestParam(value = "room", required = false) List<Integer> room,
                                                  @RequestParam(value = "floor", required = false) List<Integer> floor,
                                                  @RequestParam(value = "sharingType", required = false) List<String> sharingType,
                                                  @RequestParam("startDate") String startDate,
                                                  @RequestParam("endDate") String endDate) {
        return customersService.getCustomersDetails(hostelId, search, status, room, floor, sharingType, startDate, endDate);
    }
}
