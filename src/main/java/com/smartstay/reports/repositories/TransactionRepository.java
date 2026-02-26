package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.TransactionV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionV1, String> {
    TransactionV1 findByHostelIdAndTransactionId(String hostelId, String transactionId);

    @Query("""
            SELECT t FROM transactionv1 t WHERE t.hostelId = :hostelId AND t.customerId IN :customerIds AND t.type IS NULL 
            AND t.paymentDate = (SELECT MAX(t2.paymentDate) FROM transactionv1 t2 WHERE t2.customerId = t.customerId
            AND t2.hostelId = :hostelId)
            """)
    List<TransactionV1> findLatestPaymentsByCustomers(
            @Param("hostelId") String hostelId,
            @Param("customerIds") List<String> customerIds
    );
}
