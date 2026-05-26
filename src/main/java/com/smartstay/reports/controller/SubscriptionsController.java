package com.smartstay.reports.controller;

import com.smartstay.reports.services.SubscriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/reports/subscriptions")
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
