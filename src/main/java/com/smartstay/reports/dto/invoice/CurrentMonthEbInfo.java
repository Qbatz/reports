package com.smartstay.reports.dto.invoice;


import java.util.List;

public record CurrentMonthEbInfo(double currentMonthEbAmount,
                                 List<EBItems> ebItemsList) {
}
