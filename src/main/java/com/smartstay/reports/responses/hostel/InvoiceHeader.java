package com.smartstay.reports.responses.hostel;

public record InvoiceHeader(String startDate,
                            String endDate,
                            String totalInvoices,
                            String totalAmount,
                            String paidAmount,
                            String outstandingAmount,
                            String refundedBookingAmount,
                            String cancelledAmount) {
}
