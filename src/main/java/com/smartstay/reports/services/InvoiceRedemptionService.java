package com.smartstay.reports.services;

import com.smartstay.reports.dao.InvoiceRedemption;
import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.dto.invoice.RedeemedInfo;
import com.smartstay.reports.repositories.InvoiceRedemptionRepository;
import com.smartstay.reports.service.InvoiceService;
import com.smartstay.reports.wrappers.RedeemedInvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceRedemptionService {
    @Autowired
    private InvoiceRedemptionRepository invoiceRedemptionRepository;

    private InvoiceService invoiceService;

    @Autowired
    public void setInvoiceService(@Lazy InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public List<RedeemedInfo> findRedeemedItemsFromAdvance(String hostelId, String invoiceId) {
        List<InvoiceRedemption> listInvoiceRedemption = invoiceRedemptionRepository.findByHostelIdAndSourceId(hostelId, invoiceId);
        if (listInvoiceRedemption == null) {
            return new ArrayList<>();
        }

        List<String> targetInvoiceIds = listInvoiceRedemption
                .stream()
                .map(InvoiceRedemption::getTargetInvoiceId)
                .toList();

        List<InvoicesV1> listInvoices = invoiceService.findInvoices(targetInvoiceIds);


        return listInvoiceRedemption
                .stream()
                .map(i -> new RedeemedInvoiceMapper(listInvoices).apply(i))
                .toList();
    }
}

