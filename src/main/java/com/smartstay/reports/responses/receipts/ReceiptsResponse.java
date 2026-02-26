package com.smartstay.reports.responses.receipts;

import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;

import java.util.List;

public record ReceiptsResponse(String invoiceNumber,
                               String invoiceDate,
                               String invoiceAmount,
                               String paidAmount,
                               String dueAmount,
                               String amountInWords,
                               String title,
                               String description,
                               String labelImage,
                               List<String> headers,
                               HostelInfo hostelInfo,
                               ReceiptInfo receiptInfo,
                               TemplateInfo templateInfo,
                               CustomerInfo customerInfo) {
}
