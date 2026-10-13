package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.ExpensesV1;
import com.smartstay.reports.ennum.ExpensePaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExpensesRepository extends JpaRepository<ExpensesV1, String> {

    @Query("""
            SELECT e FROM expensesv1 e WHERE e.hostelId = :hostelId AND e.isActive = true 
            AND (:categoryIds IS NULL OR e.categoryId IN :categoryIds) 
            AND (:subCategoryIds IS NULL OR e.subCategoryId IN :subCategoryIds) 
            AND (:bankIds IS NULL OR e.bankId IN :bankIds) 
            AND (:paymentStatus IS NULL OR e.paymentStatus IN :paymentStatus) 
            AND (:vendorIds IS NULL OR e.vendorId IN :vendorIds) 
            AND (:createdByList IS NULL OR e.createdBy IN :createdByList) 
            AND (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) 
            AND (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate)) 
            ORDER BY e.transactionDate DESC 
            """)
    List<ExpensesV1> getAllExpenses(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("categoryIds") List<Long> categoryIds, @Param("subCategoryIds") List<Long> subCategoryIds, @Param("bankIds") List<String> bankIds, @Param("paymentStatus") List<ExpensePaymentStatus> paymentStatus, @Param("vendorIds") List<Integer> vendorIds, @Param("createdByList") List<String> createdByList);

}
