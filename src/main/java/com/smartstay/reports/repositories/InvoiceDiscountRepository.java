package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.InvoiceDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDiscountRepository extends JpaRepository<InvoiceDiscounts, Long> {
    @Query("""
            SELECT id FROM InvoiceDiscounts id WHERE id.hostelId=:hostelId AND id.invoiceId=:invoiceId 
            AND id.isActive=true
            """)
    InvoiceDiscounts findByHostelIdAndInvoiceId(String hostelId, String invoiceId);
}
