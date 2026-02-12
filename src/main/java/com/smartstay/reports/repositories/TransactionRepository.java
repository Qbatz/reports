package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.TransactionV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionV1, String> {
    TransactionV1 findByHostelIdAndTransactionId(String hostelId, String transactionId);
}
