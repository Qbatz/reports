package com.smartstay.reports.services;

import com.smartstay.reports.dao.SettlementItems;
import com.smartstay.reports.repositories.SettlementItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettlementItemService {
    @Autowired
    private SettlementItemRepository settlementItemRepository;

    public SettlementItems getSettlementItems(String hostelId, String customerId) {
        return settlementItemRepository.findByHostelIdAndCustomerId(hostelId, customerId);
    }

    public SettlementItems getSettlementItems(String invoiceId) {
        return settlementItemRepository.findByInvoiceId(invoiceId);
    }
}
