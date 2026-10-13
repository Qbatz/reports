package com.smartstay.reports.responses.invoice;

import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.responses.hostel.InvoiceHeader;
import com.smartstay.reports.responses.hostel.ListInvoiceItems;

import java.util.List;

public record InvoicePdfResponse(HostelInformation hostelInformation,
                                 InvoiceHeader invoiceHeader,
                                 FooterInfo footerInfo,
                                 List<ListInvoiceItems> listInvoiceItems) {
}
