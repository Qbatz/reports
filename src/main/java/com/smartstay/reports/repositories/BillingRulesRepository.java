package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingRulesRepository extends JpaRepository<BillingRules, Integer> {

    @Query("""
            select br from BillingRules br
            where br.hostel.hostelId = :hostelId
            order by br.createdAt desc, br.id desc
            limit 1
            """)
    BillingRules findLatestByHostelId(String hostelId);
}
