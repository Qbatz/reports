package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    Customers findByCustomerId(String customerId);

    @Query("SELECT c FROM Customers c WHERE c.hostelId = :hostelId AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Customers> searchCustomer(@Param("hostelId") String hostelId, @Param("search") String search);
}
