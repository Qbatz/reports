package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.TableColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TableColumnsRepository extends JpaRepository<TableColumns, Long> {

    @Query("""
            SELECT tc FROM TableColumns tc
            WHERE tc.hostelId = :hostelId
                AND tc.userId = :userId
                AND tc.moduleName = :moduleName
                AND tc.isActive = true
            """)
    TableColumns findByHostelIdAndUserId(String hostelId, String userId, String moduleName);
}
