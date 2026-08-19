package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.InvoicesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT i FROM invoicesv1 i
            WHERE i.hostelId = :hostelId 
            AND i.invoiceType != 'SETTLEMENT' 
            AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate)) 
            AND (:customerIds IS NULL OR i.customerId IN :customerIds)  
            AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate)) 
            AND (:paymentStatus IS NULL OR i.paymentStatus in (:paymentStatus)) 
            AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes) 
            AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes) 
            AND (:createdBy IS NULL OR i.createdBy IN :createdBy) 
            AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount) 
            AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount) 
            AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount) 
            AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount) 
            ORDER BY i.invoiceStartDate DESC
            """)
    List<InvoicesV1> findInvoicesByFilters(@Param("hostelId") String hostelId, 
                                          @Param("startDate") Date startDate, 
                                          @Param("endDate") Date endDate,
                                          @Param("customerIds") List<String> customerId,
                                          @Param("paymentStatus") List<String> paymentStatus, 
                                          @Param("invoiceModes") List<String> invoiceModes, 
                                          @Param("invoiceTypes") List<String> invoiceTypes, 
                                          @Param("createdBy") List<String> createdBy, 
                                          @Param("minPaidAmount") Double minPaidAmount, 
                                          @Param("maxPaidAmount") Double maxPaidAmount, 
                                          @Param("minOutstandingAmount") Double minOutstandingAmount, 
                                          @Param("maxOutstandingAmount") Double maxOutstandingAmount);

    @Query("""
            SELECT i FROM invoicesv1 i
            WHERE i.hostelId = :hostelId 
            AND i.invoiceType != 'SETTLEMENT' 
            AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
            AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
            AND i.customerId IN :customerIds
            AND (:paymentStatus IS NULL OR i.paymentStatus in (:paymentStatus) AND (:isCancelled IS NULL OR i.isCancelled=:isCancelled)) 
            AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes)
            AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes)
            AND (:createdBy IS NULL OR i.createdBy IN :createdBy)
            AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount)
            AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount)
            AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount)
            AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount)
            ORDER BY i.invoiceStartDate DESC
            """)
    List<InvoicesV1> findInvoicesByFiltersWithCustomers(@Param("hostelId") String hostelId, 
                                                        @Param("startDate") Date startDate, 
                                                        @Param("endDate") Date endDate, 
                                                        @Param("customerIds") List<String> customerIds, 
                                                        @Param("paymentStatus") List<String> paymentStatus, 
                                                        @Param("invoiceModes") List<String> invoiceModes, 
                                                        @Param("invoiceTypes") List<String> invoiceTypes, 
                                                        @Param("createdBy") List<String> createdBy, 
                                                        @Param("minPaidAmount") Double minPaidAmount, 
                                                        @Param("maxPaidAmount") Double maxPaidAmount, 
                                                        @Param("minOutstandingAmount") Double minOutstandingAmount, 
                                                        @Param("maxOutstandingAmount") Double maxOutstandingAmount, 
                                                        @Param("isCancelled") Boolean isCancelled);


    @Query("SELECT i.invoiceId FROM invoicesv1 i WHERE i.hostelId = :hostelId AND i.invoiceType IN :invoiceTypes")
    List<String> findInvoiceIdsByHostelIdAndTypeIn(@Param("hostelId") String hostelId, @Param("invoiceTypes") List<String> invoiceTypes);
}
