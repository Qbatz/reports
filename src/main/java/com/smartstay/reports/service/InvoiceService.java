package com.smartstay.reports.service;

import com.smartstay.reports.dao.CustomersBedHistory;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.ennum.PaymentStatus;
import com.smartstay.reports.repositories.InvoicesV1Repository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.invoice.*;
import com.smartstay.reports.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;

import static com.smartstay.reports.ennum.PaymentStatus.PARTIAL_REFUND;
import static com.smartstay.reports.ennum.PaymentStatus.REFUNDED;

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
        System.out.println("Total Amount: " + invoicesV1.getTotalAmount());
        System.out.println("Paid Amount: " + paidAmount);
        System.out.println("Balance Amount: " + balanceAmount);

        List<InvoiceItems> invoiceItems = invoicesV1
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

                    return new InvoiceItems(item, amount);
                })
                .toList();

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
                String.valueOf(Math.round(paidAmount)),
                String.valueOf(Math.round(balanceAmount)),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(0),
                invoiceType,
                invoiceItems,
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
}
