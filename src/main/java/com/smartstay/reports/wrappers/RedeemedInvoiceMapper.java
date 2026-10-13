package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.InvoiceRedemption;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.dto.invoice.RedeemedInfo;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class RedeemedInvoiceMapper implements Function<InvoiceRedemption, RedeemedInfo> {
    List<InvoicesV1> listInvoices = null;

    public RedeemedInvoiceMapper(List<InvoicesV1> listInvoices) {
        this.listInvoices = listInvoices;
    }

    @Override
    public RedeemedInfo apply(InvoiceRedemption invoiceRedemption) {
        String invoiceNumber = null;
        String invoiceDate = null;
        String invoiceType = null;
        Double invoiceAmount = 0.0;

        if (listInvoices != null) {
            InvoicesV1 invoicesV1 = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(invoiceRedemption.getTargetInvoiceId()))
                    .findFirst()
                    .orElse(null);

            if (invoicesV1 != null) {
                invoiceNumber = invoicesV1.getInvoiceNumber();
                invoiceDate = Utils.dateToString(invoicesV1.getInvoiceStartDate());
                invoiceAmount = invoicesV1.getTotalAmount();
                if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
                    invoiceType = "Rent";
                }
                else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
                    invoiceType = "Rent";
                }
                else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                    invoiceType = "Advance";
                }
            }
        }

        return new RedeemedInfo(invoiceRedemption.getTargetInvoiceId(),
                invoiceNumber,
                invoiceDate,
                invoiceType,
                Utils.dateToString(invoiceRedemption.getRedeemedAt()),
                invoiceRedemption.getRedemptionAmount(),
                invoiceAmount);
    }
}
