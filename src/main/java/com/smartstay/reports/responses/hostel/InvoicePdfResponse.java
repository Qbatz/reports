package com.smartstay.reports.responses.hostel;

import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;

import java.util.List;

public record InvoicePdfResponse(HostelInformation hostelInformation,
                                 FooterInfo footerInfo,
                                 InvoiceHeader invoiceHeader,
                                 List<ListInvoiceItems> listInvoiceItems) {
}
