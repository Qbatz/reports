package com.smartstay.reports.dto.invoice;


import java.util.List;

public record CurrentRentInfo(double currentMonthPaidAmount,
                              double currentMonthPayableAmount,
                              double currentMonthStayDays,
                              double currentMonthRentPayableAmount,
                              double currentMonthOtherItemAmount,
                              List<RentBreakUp> listBreakup,
                              List<CurrentMonthOtherItems> listCurrentMonthOtherItems) {
}
