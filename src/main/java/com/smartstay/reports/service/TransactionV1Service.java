package com.smartstay.reports.service;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.ennum.InvoiceItems;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.ennum.TransactionType;
import com.smartstay.reports.repositories.TransactionRepository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.invoice.BedInfo;
import com.smartstay.reports.responses.receipts.*;
import com.smartstay.reports.utils.AmountToWordsUtils;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.wrappers.ReceiptReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private UsersService userService;

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
        String description = null;
        List<String> headers = null;
        String labelUrl = null;
        BedInfo bedInfo = null;

        BookingsV1 bookingsV1 = bookingsService.findByCustomerId(invoicesV1.getCustomerId(), invoicesV1.getHostelId());
        if (bookingsV1 != null) {
            if (bookingsV1.getBedId() != 0) {
                bedInfo = bedsService.getBedDetails(bookingsV1.getBedId());
            }

        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            title = "Payment Receipt";
            description = "Payment";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            title = "Final Settlement Receipt";
            description = "Settlement";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            description = "Security Deposit (Advance)";
            title = "Security Deposit (Advance)";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            description = "Security Deposit (Advance)";
            title = "Booking Receipt";
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())
                || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())
                || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
           headers = new ArrayList<>();
           headers.add("Sl No");
           headers.add("Description");
           headers.add("Amount");
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            headers = new ArrayList<>();
            headers.add("Invoice No");
            headers.add("Invoice Date");
            headers.add("Invoice Amount");
            headers.add("Payment Amount");
        }

        if (transactionV1.getType() != null) {
            if (transactionV1.getType().equalsIgnoreCase(TransactionType.REFUND.name())) {
                labelUrl = "https://smartstaydevs.s3.ap-south-1.amazonaws.com/smartstay/ss-refunded.png";
            }
            else {
                labelUrl = "https://smartstaydevs.s3.ap-south-1.amazonaws.com/smartstay/ss-payment-received.png";
            }
        }

        else {
            labelUrl = "https://smartstaydevs.s3.ap-south-1.amazonaws.com/smartstay/ss-payment-received.png";
        }

        String paidAmountInWords = AmountToWordsUtils.convert(transactionV1.getPaidAmount()) + " Rupees Only";
        String paymentMode = bankingService.getPaymentMode(transactionV1.getBankId());
        ReceiptInfo receiptInfo = new ReceiptInfo(transactionV1.getTransactionReferenceId(),
                Utils.dateToString(transactionV1.getPaymentDate()),
                Utils.dateToTime(transactionV1.getPaymentDate()),
                paymentMode,
                transactionV1.getReferenceNumber());
        TemplateInfo templateInfo = templateService.getReceiptTemplate(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());

        double totalAmount = 0.0;
        double paidAmount = 0.0;
        double dueAmount = 0.0;
        if (invoicesV1.getTotalAmount() != null) {
            totalAmount = invoicesV1.getTotalAmount();
        }
        if (transactionV1.getPaidAmount() != null) {
            paidAmount = transactionV1.getPaidAmount();
        }
//        if (invoicesV1.getPaidAmount() != null) {
//            paidAmount = transactionV1.getPaidAmount();
//        }
        dueAmount = totalAmount - paidAmount;

        return new ReceiptsResponse(invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                String.valueOf(Math.round(totalAmount)),
                String.valueOf(Math.round(paidAmount)),
                String.valueOf(Math.round(dueAmount)),
                paidAmountInWords,
                title,
                description,
                labelUrl,
                headers,
                hostelInfo,
                receiptInfo,
                templateInfo,
                customerInfo, bedInfo);
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

    public List<TransactionV1> findTransactions(String hostelId, List<String> customerIds) {
        List<TransactionV1> listTransactions = transactionRepository.findLatestPaymentsByCustomers(hostelId, customerIds);
        if (listTransactions == null) {
            listTransactions = new ArrayList<>();
        }
        return listTransactions;
    }

    public ResponseEntity<?> getReceiptReports(String hostelId, String startDate, String endDate) {
        ReceiptsReports receiptsReports = getReceiptReportDetails(hostelId, startDate, endDate);

        Context context = new Context();
        context.setVariable("receipts", receiptsReports);

        String receiptReportUrl = pdfServices.generateReceiptReportPDF("receipt-report", context);

        return new ResponseEntity<>(receiptReportUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getReceiptReportsDetails(String hostelId, String startDate, String endDate) {
        ReceiptsReports receiptsReports = getReceiptReportDetails(hostelId, startDate, endDate);
        return new ResponseEntity<>(receiptsReports, HttpStatus.OK);
    }

    public ReceiptsReports getReceiptReportDetails(String hostelId, String startDate, String endDate) {
        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);
        Date sDate = Utils.stringToDate(startDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        List<TransactionV1> listTransactions = transactionRepository.getTransactionsList(hostelId, sDate, eDate);
        ReceiptHeader receiptHeader = null;
        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));

        ReceiptsReports receiptsReports = null;
        if (listTransactions != null) {
            double receivedAmount = listTransactions
                    .stream()
                    .filter(i ->  i.getType() == null)
                    .mapToDouble(TransactionV1::getPaidAmount)
                    .sum();

            double returnedAmount = listTransactions
                    .stream()
                    .filter(i ->  i.getType() != null && i.getType().equalsIgnoreCase(TransactionType.REFUND.name()))
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() < 0) {
                            return i.getPaidAmount() * -1;
                        }
                        return i.getPaidAmount();
                    })
                    .sum();
            receiptHeader = new ReceiptHeader(String.valueOf(receivedAmount),
                    String.valueOf(returnedAmount),
                    String.valueOf(listTransactions.size()),
                    Utils.dateToString(sDate),
                    Utils.dateToString(eDate));

            List<String> listInvoicesId = listTransactions
                    .stream()
                    .map(TransactionV1::getInvoiceId)
                    .distinct()
                    .toList();
            List<InvoicesV1> listInvoices = invoiceService.getInvoicesByIds(listInvoicesId);

            List<String> customerIds = listTransactions.stream().map(TransactionV1::getCustomerId).filter(Objects::nonNull)
                    .distinct().toList();
            List<String> tBankIds = listTransactions.stream().map(TransactionV1::getBankId).filter(Objects::nonNull).distinct()
                    .toList();
            List<String> tUserIds = listTransactions.stream().map(TransactionV1::getCreatedBy).filter(Objects::nonNull)
                    .distinct().toList();

            List<Customers> listCustomers = customerServices.findByCustomerIds(customerIds);
            List<BankingV1> listBanks = bankingService.findByBankIds(tBankIds);
            List<Users> listUsers = userService.findByUserIds(tUserIds);

            List<ReceiptList> listReceipts = listTransactions
                    .stream()
                    .map(i -> new ReceiptReportMapper(listCustomers, listUsers, listBanks, listInvoices).apply(i))
                    .toList();


            receiptsReports = new ReceiptsReports(hostelInformation,
                    receiptHeader,
                    footerInfo,
                    listReceipts);
        }

        return receiptsReports;
    }


}
