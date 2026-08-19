package com.smartstay.reports.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetainerItems {
    String invoiceId;
    String invoiceNo;
    String invoiceDate;
    Double totalAmount;
    //available amount
    Double amount;
}
