package com.smartstay.reports.service;

import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.dao.CustomersBedHistory;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.dto.customer.Deductions;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.ennum.PaymentStatus;
import com.smartstay.reports.repositories.InvoicesV1Repository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.InvoiceHeader;
import com.smartstay.reports.responses.hostel.ListInvoiceItems;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.invoice.*;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.wrappers.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartstay.reports.ennum.PaymentStatus.*;

@Service
public class InvoiceService {
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private PDFServices invoicePDFServices;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomerServices customerServices;
    @Autowired
    private CustomerBedHistoryService customerBedHistoryService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private PDFServices pdfServices;

    public ResponseEntity<?> getInvoiceReport(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        InvoiceInfo invoiceInfo = getInvoiceInfo(invoicesV1);
        Context context = new Context();
        context.setVariable("invoice", invoiceInfo);

        String invoiceUrl = invoicePDFServices.generatePdf(invoiceId, "invoice", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceDetails(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        InvoiceInfo invoiceInfo = getInvoiceInfo(invoicesV1);

        return new ResponseEntity<>(invoiceInfo, HttpStatus.OK);
    }

    public InvoiceInfo getInvoiceInfo(InvoicesV1 invoicesV1) {
        double paidAmount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }
        double balanceAmount = calculateBalance(invoicesV1.getTotalAmount(), paidAmount, invoicesV1.getPaymentStatus());
        double totalDeductionAmount = 0.0;
        List<InvoiceItems> invoiceItems = new ArrayList<>();
        List<Deductions> listDeductions = new ArrayList<>();

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {

            totalDeductionAmount = invoicesV1.getInvoiceItems()
                    .stream()
                    .mapToDouble(com.smartstay.reports.dao.InvoiceItems::getAmount)
                    .sum();
            listDeductions = invoicesV1.getInvoiceItems().stream().map(i -> {
                Deductions d = new Deductions();
                if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                    if (i.getOtherItem() != null) {
                        i.setInvoiceItem(i.getOtherItem());
                    }
                } else {
                    i.setInvoiceItem(i.getInvoiceItem());
                }
                d.setType(i.getInvoiceItem());
                d.setAmount(i.getAmount());

                return d;
            }).toList();

            invoiceItems.add(new InvoiceItems(InvoiceType.SETTLEMENT.name(), String.valueOf(invoicesV1.getBasePrice()), invoicesV1.getInvoiceNumber()));
        }
        else {
            invoiceItems = invoicesV1
                    .getInvoiceItems()
                    .stream()
                    .map(i -> {
                        String item = null;
                        String amount = null;
                        if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.RENT.name())) {
                            item = "Rent";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.EB.name())) {
                            item = "Electricity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.AMENITY.name())) {
                            item = "Amenity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.BOOKING.name())) {
                            item = "Security Deposit (Advance)";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.ADVANCE.name())) {
                            item = "Security Deposit (Advance)";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.MAINTENANCE.name())) {
                            item = "Maintenance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                            item = i.getOtherItem();
                        }

                        amount = String.valueOf(Math.round(i.getAmount()));

                        return new InvoiceItems(item, amount, invoicesV1.getInvoiceNumber());
                    })
                    .toList();
        }

        String invoiceType = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Payment Bill";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Security Deposit(Booking)";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Security Deposit";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";

        }


        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        CustomersBedHistory cbh = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        BedInfo bedInfo = bedsService.getBedDetails(cbh.getBedId());
        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());

        String rentalPeriod = Utils.dateToMonth(invoicesV1.getInvoiceStartDate()) + "-" + Utils.dateToMonth(invoicesV1.getInvoiceEndDate());
        InvoiceInfo invoiceInfo = new InvoiceInfo(
                invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                rentalPeriod,
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(totalDeductionAmount)),
                String.valueOf(Math.round(paidAmount)),
                String.valueOf(Math.round(balanceAmount)),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(0),
                invoiceType,
                invoiceItems,
                listDeductions,
                hostelInfo,
                customerInfo,
                bedInfo,
                templateInfo
        );

        return invoiceInfo;
    }

    private double calculateBalance(double totalAmount, double paidAmount,
                                    String paymentStatus) {
        return switch (paymentStatus) {
            case "PARTIAL_REFUND", "REFUND" -> totalAmount + paidAmount;

            default -> totalAmount - paidAmount;
        };
    }

    public InvoicesV1 getInvoice(String invoiceId) {
        return invoicesV1Repository.getReferenceById(invoiceId);
    }

    public List<InvoicesV1> getInvoicesByIds(List<String> listInvoicesId) {
        return invoicesV1Repository.findAllById(listInvoicesId);
    }

    public ResponseEntity<?> getInvoiceReportDetailDetails(String hostelId, String startDate, String endDate) {
        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, startDate, endDate);
        return new ResponseEntity<>(invoicePdfResponse, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReport(String hostelId, String startDate, String endDate) {
        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, startDate, endDate);
        Context context = new Context();
        context.setVariable("invoices", invoicePdfResponse);

        String invoiceReportUrl = pdfServices.generateReceiptReportPDF("invoice-report", context);
        return new ResponseEntity<>(invoiceReportUrl, HttpStatus.OK);
    }

    public InvoicePdfResponse getInvoiceDetails(String hostelId, String startDate, String endDate) {
        Date sDate = Utils.stringToDate(startDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        List<InvoicesV1> listInvoices = invoicesV1Repository.findByHostelId(hostelId, sDate, eDate);
        List<String> customerIds = listInvoices
                .stream()
                .map(InvoicesV1::getCustomerId)
                .toList();

        List<Customers> customers = customerServices.findByCustomerIds(customerIds);

        List<ListInvoiceItems> listInvoiceItems = listInvoices
                .stream()
                .map(i -> new InvoiceMapper(customers).apply(i))
                .toList();

        String totalInvoice = String.valueOf(listInvoices.size());
        double totalInvoiceAmount = listInvoices.
                stream()
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) {
                        return 0.0;
                    }
                    else {
                        if (i.getTotalAmount() < 0) {
                            return i.getTotalAmount() * -1;
                        }
                        else {
                            return i.getTotalAmount();
                        }
                    }
                })
                .sum();
        double paidAmount = listInvoices
                .stream()
                .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) ||
                        i.getPaymentStatus().equalsIgnoreCase(PARTIAL_PAYMENT.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    else {
                        return 0.0;
                    }
                })
                .sum();

        double returnInvoiceAmount = listInvoices
                .stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    return i.getTotalAmount();
                })
                .sum();

        double cancelledAmount = listInvoices
                .stream()
                .filter(InvoicesV1::isCancelled)
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();

        double outstandingAmount = listInvoices
                .stream()
                .filter(i -> !i.isCancelled())
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();

        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));
        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);
        InvoiceHeader invoiceHeader = new InvoiceHeader(Utils.dateToString(sDate), Utils.dateToString(eDate),
                totalInvoice,
                String.valueOf(totalInvoiceAmount),
                String.valueOf(paidAmount),
                String.valueOf(outstandingAmount),
                String.valueOf(returnInvoiceAmount),
                String.valueOf(cancelledAmount));

        return new InvoicePdfResponse(hostelInformation,
                invoiceHeader,
                footerInfo,
                listInvoiceItems);
    }
}
