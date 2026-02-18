package com.smartstay.reports.responses.hostel;

public record TemplateInfo(String colorCode,
                           String hostelLogo,
                           String qrCode,
                           String phone,
                           String emailId,
                           String signature,
                           String termsAndCondition,
                           String notes) {
}
