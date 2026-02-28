package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.ExpensesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExpensesRepository extends JpaRepository<ExpensesV1, String> {

    @Query("""
            SELECT e FROM expensesv1 e WHERE e.hostelId = :hostelId AND e.isActive = true AND 
            (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) AND 
            (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate)) ORDER BY e.transactionDate DESC
            """)
    List<ExpensesV1> getAllExpenses(String hostelId, Date startDate, Date endDate);
}
