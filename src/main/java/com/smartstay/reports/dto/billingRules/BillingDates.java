package com.smartstay.reports.dto.billingRules;

import java.util.Date;

public record BillingDates(Date currentBillStartDate,
                           Date currentBillEndDate,
                           Date dueDate,
                           Integer dueDays,
                           boolean hasGracePeriod,
                           Integer gracePeriodDays,
                           String typeOfBilling,
                           String billingModel) {
}