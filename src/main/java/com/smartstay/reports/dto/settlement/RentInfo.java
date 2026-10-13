package com.smartstay.reports.dto.settlement;

import com.smartstay.reports.dto.invoice.CurrentMonthOtherItems;
import com.smartstay.reports.dto.invoice.RentBreakUp;

import java.util.List;

public record RentInfo(Double currentPayableRent,
                       Double currentRentPaid,
                       Integer stayDays,
                       Double currentMonthRent,
                       Double currentMonthTotalAmount,
                       Double currentMonthPayableAmount,
                       String currentInvoiceStartDate,
                       String currentInvoiceEndDate,
                       String currentInvoiceId,
                       Double otherItemAmount,
                       boolean isDiscountApplied,
                       Double discountAmount,
                       Double fullRent,
                       Double rentDifference,
                       List<CurrentMonthOtherItems> currentMonthOtherItems,
                       List<RentBreakUp> rentLists) {
}
