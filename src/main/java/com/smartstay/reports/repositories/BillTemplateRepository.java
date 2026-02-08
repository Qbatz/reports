package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BillTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillTemplateRepository extends JpaRepository<BillTemplates, Integer> {
    BillTemplates getByHostelId(String hostelId);
}
