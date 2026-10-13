package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.ennum.PaymentStatus;
import com.smartstay.reports.responses.hostel.ListInvoiceItems;
import com.smartstay.reports.utils.NameUtils;
import com.smartstay.reports.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceMapper implements Function<InvoicesV1, ListInvoiceItems> {
    List<Customers> listCustomers = null;

    public InvoiceMapper(List<Customers> listCustomers) {
        this.listCustomers = listCustomers;
    }

    @Override
    public ListInvoiceItems apply(InvoicesV1 invoicesV1) {
        String name = "NA";
        String invoiceType = "NA";
        String dueAmount = "NA";
        String paymentStatus = "NA";

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Re-Assign Rent";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
        }

        if (!invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            double dAmount = 0.0;
            if (invoicesV1.getPaidAmount() != null) {
                dAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();
                dueAmount = String.valueOf(dAmount);
            }
            else {
                dueAmount = String.valueOf(invoicesV1.getTotalAmount());
            }
        }
        else {
            if (invoicesV1.getTotalAmount() < 0) {
                double dAmount = 0.0;
                if (invoicesV1.getPaidAmount() != null) {
                    dAmount = invoicesV1.getTotalAmount() + invoicesV1.getPaidAmount();
                    dueAmount = String.valueOf(dAmount);
                }else {
                    dAmount = invoicesV1.getTotalAmount();
                    dueAmount = String.valueOf(dAmount);
                }
            }
            else {
                double dAmount = 0.0;
                if (invoicesV1.getPaidAmount() != null) {
                    dAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();
                    dueAmount = String.valueOf(dAmount);
                }
                else {
                    dueAmount = String.valueOf(invoicesV1.getTotalAmount());
                }
            }
        }

        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
            paymentStatus = "Paid";
        }
        else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            paymentStatus = "Partially Paid";
        }
        else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
            paymentStatus = "Refunded";
        }
        else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {
            paymentStatus = "Partially Refunded";
        }
        else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
            paymentStatus = "Pending";
        }
        else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name())) {
            paymentStatus = "Cancelled";
        }

        if (invoicesV1.isCancelled()) {
            paymentStatus = "Cancelled";
        }
        if (listCustomers != null) {
            Customers customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (customers != null) {
                name = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
            }
        }
        return new ListInvoiceItems(invoicesV1.getInvoiceNumber(),
                name,
                invoiceType,
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                String.valueOf(invoicesV1.getTotalAmount()),
                dueAmount,
                paymentStatus);
    }
}
