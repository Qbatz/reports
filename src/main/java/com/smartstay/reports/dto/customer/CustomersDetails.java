package com.smartstay.reports.dto.customer;

import java.util.List;

public record CustomersDetails(HostelInformation hostelInformation, HeaderInfo headerInfo, List<Customers> customersList) {
}
