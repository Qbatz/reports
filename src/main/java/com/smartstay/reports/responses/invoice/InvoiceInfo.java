package com.smartstay.reports.responses.invoice;

import com.smartstay.reports.dto.customer.Deductions;
import com.smartstay.reports.dto.invoice.AccountDetails;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;

import java.util.List;

public record InvoiceInfo(String invoiceNumber,
                          String invoiceDate,
                          String dueDate,
                          String rentalPeriod,
                          String payableAmount,
                          String totalDeductionAmount,
                          String paidAmount,
                          String balanceAmount,
                          String basePrice,
                          String totalAmount,
                          String discount,
                          String invoiceType,
                          String paymentStatus,
                          List<InvoiceItems> invoiceItems,
                          List<Deductions> listDeductions,
                          HostelInfo hostelInfo,
                          CustomerInfo customerInfo,
                          BedInfo bedInfo,
                          TemplateInfo templateInfo,
                          AccountDetails accountInfo) {

}
