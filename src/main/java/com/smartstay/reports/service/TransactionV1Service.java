package com.smartstay.reports.service;

import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.dao.TransactionV1;
import com.smartstay.reports.ennum.InvoiceItems;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.repositories.TransactionRepository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.receipts.ReceiptInfo;
import com.smartstay.reports.responses.receipts.ReceiptsResponse;
import com.smartstay.reports.utils.AmountToWordsUtils;
import com.smartstay.reports.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
public class TransactionV1Service {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private CustomerServices customerServices;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private PDFServices pdfServices;

    public ResponseEntity<?> getReceiptPDF(String hostelId, String transactionId) {
        TransactionV1 transactionV1 = transactionRepository.findByHostelIdAndTransactionId(hostelId, transactionId);
        if (transactionV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 invoicesV1 = invoiceService.getInvoice(transactionV1.getInvoiceId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReceiptsResponse receiptsResponse = getReceiptInfo(transactionV1, invoicesV1);
        Context context = new Context();
        context.setVariable("receipt", receiptsResponse);

        String receiptsUrl = pdfServices.generateReceiptPdf("receipt", context);
        return new ResponseEntity<>(receiptsUrl, HttpStatus.OK);
    }

    public ReceiptsResponse getReceiptInfo(TransactionV1 transactionV1, InvoicesV1 invoicesV1) {
        String title = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            title = "Payment Receipt";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            title = "Settlement";
        }

        String paidAmountInWords = AmountToWordsUtils.convert(transactionV1.getPaidAmount()) + " Only";
        String paymentMode = bankingService.getPaymentMode(transactionV1.getBankId());
        ReceiptInfo receiptInfo = new ReceiptInfo(transactionV1.getTransactionReferenceId(),
                Utils.dateToString(transactionV1.getPaymentDate()),
                Utils.dateToTime(transactionV1.getPaymentDate()),
                paymentMode,
                transactionV1.getReferenceNumber());
        TemplateInfo templateInfo = templateService.getReceiptTemplate(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());

        return new ReceiptsResponse(invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(transactionV1.getPaidAmount())),
                paidAmountInWords,
                title,
                hostelInfo,
                receiptInfo,
                templateInfo,
                customerInfo);
    }


    public ResponseEntity<?> getReceiptDetails(String hostelId, String transactionId) {
        TransactionV1 transactionV1 = transactionRepository.findByHostelIdAndTransactionId(hostelId, transactionId);
        if (transactionV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 invoicesV1 = invoiceService.getInvoice(transactionV1.getInvoiceId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReceiptsResponse response = getReceiptInfo(transactionV1, invoicesV1);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
