package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BookingsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {

    @Query(value = """
                        SELECT b FROM bookingsv1 b WHERE b.hostelId = :hostelId 
                        AND 
                (b.joiningDate IS NOT NULL AND DATE(b.joiningDate) <= DATE(:endDate) 
                OR 
                (b.joiningDate IS NULL AND DATE(b.expectedJoiningDate) <= DATE(:endDate))) 
                AND 
                (b.currentStatus <> 'CANCELLED' OR (b.currentStatus = 'CANCELLED' AND DATE(b.cancelDate) >= DATE(:startDate) AND 
                DATE(b.cancelDate) <= DATE(:endDate))) AND 
                (b.checkoutDate IS NULL OR DATE(b.checkoutDate) >= DATE(:startDate)) """)
    List<BookingsV1> findAllBookingsWithFilters(@Param("hostelId") String hostelId,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate);

    BookingsV1 findByCustomerIdAndHostelId(@Param("customerId") String customerId,
                                           @Param("hostelId") String hostelId);
}
