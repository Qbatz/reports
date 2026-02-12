package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BankingV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingRepository extends JpaRepository<BankingV1, String> {
}
