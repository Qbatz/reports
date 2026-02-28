package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.ennum.TransactionType;
import com.smartstay.reports.responses.receipts.ReceiptList;
import com.smartstay.reports.utils.NameUtils;
import com.smartstay.reports.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class ReceiptReportMapper implements Function<TransactionV1, ReceiptList> {

    List<Customers> listCustomers = null;
    List<Users> listCreatedBy = null;
    List<BankingV1> listBanks = null;
    List<InvoicesV1> listInvoices = null;

    public ReceiptReportMapper(List<Customers> listCustomers, List<Users> listCreatedBy, List<BankingV1> listBanks, List<InvoicesV1> listInvoices) {
        this.listCustomers = listCustomers;
        this.listCreatedBy = listCreatedBy;
        this.listBanks = listBanks;
        this.listInvoices = listInvoices;
    }

    @Override
    public ReceiptList apply(TransactionV1 transactionV1) {
        String receiptNo = transactionV1.getTransactionReferenceId();
        String paidDate = Utils.dateToString(transactionV1.getPaymentDate());
        String tenantName = null;
        String invoiceNumber = "NA";
        String paidAmount = String.valueOf(transactionV1.getPaidAmount());
        String paymentMode = null;
        String collectedBy = null;
        String paymentType = "Credit";

        if (transactionV1.getType() != null && transactionV1.getType().equalsIgnoreCase(TransactionType.REFUND.name())) {
            paymentType = "Debit";
        }

        if (listCustomers != null) {
            Customers customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(transactionV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);

            if (customers != null) {
                tenantName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
            }
        }

        if (listBanks != null) {
            BankingV1 bankingV1 = listBanks.stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                    .findFirst()
                    .orElse(null);
            if (bankingV1 != null) {
                paymentMode = bankingV1.getAccountType();
            }
        }

        if (listCreatedBy != null) {
            Users users = listCreatedBy
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(transactionV1.getCreatedBy()))
                    .findFirst()
                    .orElse(null);
            if (users != null) {
                collectedBy = NameUtils.getFullName(users.getFirstName(), users.getLastName());
            }
        }

        if (listInvoices != null) {
            InvoicesV1 invoicesV1 = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(transactionV1.getInvoiceId()))
                    .findFirst()
                    .orElse(null);
            if (invoicesV1 != null) {
                invoiceNumber = invoicesV1.getInvoiceNumber();
            }
        }


        return new ReceiptList(receiptNo,
                paidDate,
                tenantName,
                invoiceNumber,
                paidAmount,
                paymentMode,
                collectedBy,
                paymentType);
    }
}
