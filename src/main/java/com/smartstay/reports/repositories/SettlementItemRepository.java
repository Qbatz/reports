package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.SettlementItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItems, Long> {
    SettlementItems findByHostelIdAndCustomerId(String hostelId, String customerId);
    SettlementItems findByInvoiceId(String invoiceId);
}
