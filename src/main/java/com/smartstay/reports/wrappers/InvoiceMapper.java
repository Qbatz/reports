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

        if (InvoiceType.RENT.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Rent";
        } else if (InvoiceType.REASSIGN_RENT.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Re-Assign Rent";
        } else if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Booking";
        } else if (InvoiceType.ADVANCE.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Advance";
        } else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Additional Advance";
        } else if (InvoiceType.SETTLEMENT.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Settlement";
        } else if (InvoiceType.EB.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "EB";
        } else if (InvoiceType.OTHER.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Other";
        } else if (InvoiceType.AMOUNT_HOLDING.name().equalsIgnoreCase(invoicesV1.getInvoiceType()) ||
                InvoiceType.EB_HOLDING.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Retainer";
        } else {
            invoiceType = Utils.capitalize(invoicesV1.getInvoiceType());
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

        if (PaymentStatus.PAID.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Paid";
        } else if (PaymentStatus.PARTIAL_PAYMENT.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Partially Paid";
        } else if (PaymentStatus.REFUNDED.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Refunded";
        } else if (PaymentStatus.PARTIAL_REFUND.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Partially Refunded";
        } else if (PaymentStatus.PENDING.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Pending";
        } else if (PaymentStatus.PENDING_REFUND.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Pending Refunded";
        } else if (PaymentStatus.CANCELLED.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Cancelled";
        } else if (PaymentStatus.ADVANCE_IN_HAND.name().equalsIgnoreCase(invoicesV1.getPaymentStatus())) {
            paymentStatus = "Advance In Hand";
        } else {
            paymentStatus = Utils.capitalize(invoicesV1.getPaymentStatus());
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
