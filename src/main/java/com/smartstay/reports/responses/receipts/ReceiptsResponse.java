package com.smartstay.reports.responses.receipts;

import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;

public record ReceiptsResponse(String invoiceNumber,
                               String invoiceDate,
                               String invoiceAmount,
                               String paidAmount,
                               String amountInWords,
                               String title,
                               HostelInfo hostelInfo,
                               ReceiptInfo receiptInfo,
                               TemplateInfo templateInfo,
                               CustomerInfo customerInfo) {
}
