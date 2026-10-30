package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.BankingV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BankingV2Repository extends JpaRepository<BankingV2, String> {

    List<BankingV2> findAllByBankIdInAndIsActiveTrueAndIsDeletedFalse(Set<String> bankIds);

    BankingV2 findByBankIdAndIsActiveTrueAndIsDeletedFalse(String bankId);

    @Query("""
            SELECT b.bankId FROM bankingv2 b
            WHERE b.hostelId = :hostelId
                AND b.accountType IN :accountTypes
                AND b.isActive = true
                AND b.isDeleted = false
            """)
    List<String> findBankIdsByAccountTypes(@Param("hostelId") String hostelId,
                                           @Param("accountTypes") List<String> accountTypes);
}
