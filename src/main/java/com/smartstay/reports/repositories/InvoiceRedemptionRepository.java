package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.InvoiceRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRedemptionRepository extends JpaRepository<InvoiceRedemption, Long> {
    @Query("""
            SELECT ir FROM InvoiceRedemption ir WHERE ir.hostelId=:hostelId AND ir.sourceInvoiceId=:invoiceId AND 
            ir.isActive = true
            """)
    List<InvoiceRedemption> findByHostelIdAndSourceId(String hostelId, String invoiceId);
}
