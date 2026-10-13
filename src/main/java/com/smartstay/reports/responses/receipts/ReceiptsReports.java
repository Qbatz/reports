package com.smartstay.reports.responses.receipts;

import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HeaderInfo;
import com.smartstay.reports.dto.customer.HostelInformation;

import java.util.List;

public record ReceiptsReports(HostelInformation hostelInformation,
                              ReceiptHeader headerInfo,
                              FooterInfo footerInfo,
                              List<ReceiptList> listReceipts) {
}
