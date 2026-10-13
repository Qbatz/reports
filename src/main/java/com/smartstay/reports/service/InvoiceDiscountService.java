package com.smartstay.reports.service;

import com.smartstay.reports.dao.InvoiceDiscounts;
import com.smartstay.reports.repositories.InvoiceDiscountRepository;
import com.smartstay.reports.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceDiscountService {

    @Autowired
    private InvoiceDiscountRepository invoiceDiscountRepository;
    public double getInoiceDiscount(String hostelId, String invoiceId) {
        InvoiceDiscounts invoiceDiscounts = invoiceDiscountRepository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoiceDiscounts != null) {
            return Utils.roundOffWithTwoDigit(invoiceDiscounts.getDiscountAmount());
        }
        return 0.0;
    }
}
