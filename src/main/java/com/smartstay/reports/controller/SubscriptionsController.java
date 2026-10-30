package com.smartstay.reports.controller;

import com.smartstay.reports.services.SubscriptionsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/reports/subscriptions")
@CrossOrigin("*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class SubscriptionsController {

    @Autowired
    private SubscriptionsService subscriptionsService;

    @GetMapping("/details/{hostelId}/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionDetails(
            @PathVariable("hostelId") String hostelId,
            @PathVariable("subscriptionId") String subscriptionId) {
        return subscriptionsService.getSubscriptionDetails(hostelId, subscriptionId);
    }

    @GetMapping("/{hostelId}/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionPdf(
            @PathVariable("hostelId") String hostelId,
            @PathVariable("subscriptionId") String subscriptionId) {
        return subscriptionsService.getSubscriptionPdf(hostelId, subscriptionId);
    }
}
