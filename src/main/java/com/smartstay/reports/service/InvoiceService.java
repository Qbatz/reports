package com.smartstay.reports.service;

import com.smartstay.reports.dao.CustomersBedHistory;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.repositories.InvoicesV1Repository;
import com.smartstay.reports.responses.invoice.*;
import com.smartstay.reports.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class InvoiceService {
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private InvoicePDFServices invoicePDFServices;
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
        double paidAmount = invoicesV1.getPaidAmount();
        double balanceAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();

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

        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        CustomersBedHistory cbh = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        BedInfo bedInfo = bedsService.getBedDetails(cbh.getBedId());
        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId());

        String rentalPeriod = Utils.dateToMonth(invoicesV1.getInvoiceStartDate()) + "-" + Utils.dateToMonth(invoicesV1.getInvoiceEndDate());
        InvoiceInfo invoiceInfo = new InvoiceInfo(
                invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                rentalPeriod,
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(invoicesV1.getPaidAmount())),
                String.valueOf(Math.round(balanceAmount)),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(0),
                "Rental",
                invoiceItems,
                hostelInfo,
                customerInfo,
                bedInfo,
                templateInfo
        );

        return invoiceInfo;
    }

    public InvoicesV1 getInvoice(String invoiceId) {
        return invoicesV1Repository.getReferenceById(invoiceId);
    }
}
