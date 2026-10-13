package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.VendorV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorV1Repository extends JpaRepository<VendorV1, Integer> {
    @Query("""
            SELECT ven FROM vendorv1 ven WHERE ven.vendorId IN (:vendorIds)
            """)
    List<VendorV1> findByVendorIds(List<Integer> vendorIds);
}
