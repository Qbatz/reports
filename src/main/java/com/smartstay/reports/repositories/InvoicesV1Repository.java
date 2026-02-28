package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.InvoicesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String>  {
    InvoicesV1 findByHostelIdAndInvoiceId(String hostelId, String invoiceId);

    @Query("""
             SELECT i FROM invoicesv1 i WHERE i.hostelId = :hostelId AND i.invoiceType != 'SETTLEMENT'  
              AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate)) 
              AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
            """)
    List<InvoicesV1> findByHostelId(String hostelId, Date startDate, Date endDate);
}
