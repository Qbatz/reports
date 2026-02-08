package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface CustomerBedHistoryRepository extends JpaRepository<CustomersBedHistory, Long> {
    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and DATE(start_date) <= DATE(:endDate) and (end_date IS NULL OR DATE(end_date) >= DATE(:startDate))
            ORDER BY start_date DESC LIMIT 1
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
