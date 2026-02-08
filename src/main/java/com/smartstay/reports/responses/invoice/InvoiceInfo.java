package com.smartstay.reports.responses.invoice;

import java.util.List;

public record InvoiceInfo(String invoiceNumber,
                          String invoiceDate,
                          String dueDate,
                          String rentalPeriod,
                          String payableAmount,
                          String paidAmount,
                          String balanceAmount,
                          String totalAmount,
                          String discount,
                          List<InvoiceItems> invoiceItems,
                          HostelInfo hostelInfo,
                          CustomerInfo customerInfo,
                          BedInfo bedInfo,
                          TemplateInfo templateInfo) {

}
