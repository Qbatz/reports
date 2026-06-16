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

    @Query("""
            SELECT i FROM invoicesv1 i WHERE i.customerId = :customerId AND i.invoiceType='ADVANCE' 
            """)
    InvoicesV1 findAdvanceInvoiceByCustomerId(String customerId);

    List<InvoicesV1> findByInvoiceIdIn(List<String> invoiceId);

    @Query("""
           SELECT i FROM invoicesv1 i WHERE i.invoiceType = 'BOOKING' AND i.paymentStatus='PAID' AND i.isCancelled=false AND 
           i.hostelId=:hostelId AND i.customerId=:customerId
            """)
    InvoicesV1 findBookingInvoice(String hostelId, String customerId);
}
