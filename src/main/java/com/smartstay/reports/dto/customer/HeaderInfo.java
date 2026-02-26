package com.smartstay.reports.dto.customer;

public record HeaderInfo(String totalTenantsCount,
                         String activeTenantsCount,
                         String noticePeriodCount,
                         String checkoutCount,
                         String inactiveCount,
                         String bookingCount,
                         String startDate,
                         String endDate) {
}
