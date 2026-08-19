package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BankingV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingRepository extends JpaRepository<BankingV1, String> {
    @Query("SELECT b.bankId FROM bankingv1 b WHERE b.hostelId = :hostelId AND b.accountType IN :accountTypes AND b.isDeleted = false")
    List<String> findBankIdsByAccountTypes(@Param("hostelId") String hostelId,
                                           @Param("accountTypes") List<String> accountTypes);
}
