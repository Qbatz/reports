package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlansRepository extends JpaRepository<Plans, String> {
    Plans findPlanByPlanCode(String planCode);
}
