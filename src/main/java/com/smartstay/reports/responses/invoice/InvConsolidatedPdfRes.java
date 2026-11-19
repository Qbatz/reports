package com.smartstay.reports.responses.invoice;

import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.responses.hostel.InvoiceHeader;
import com.smartstay.reports.responses.pagination.PaginationDataRes;

import java.util.List;

public record InvConsolidatedPdfRes(boolean includePaidInvoice,
                                    boolean includeEmptyBeds,
                                    boolean includeSubTotals,
                                    boolean includeGrandTotal,
                                    boolean includeHostelName,
                                    HostelInformation hostelInformation,
                                    InvoiceHeader invoiceHeader,
                                    FooterInfo footerInfo,
                                    List<InvFloorsRes> floors,
                                    List<InvRoomRes> rooms,
                                    InvConsolidatedFiltersRes filters,
                                    PaginationDataRes paginationData) {
}
