package com.smartstay.reports.dto.customer;

public record CustomerInfo(String firstName,
                           String lastName,
                           String fullName,
                           String profilePic,
                           String initials,
                           String mobile,
                           String status,
                           String lastPayment,
                           String rent) {
}
