package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, String> {

    @Query("""
            SELECT c FROM Credentials c
            WHERE c.service = :service
            """)
    Credentials findByService(String service);
}
