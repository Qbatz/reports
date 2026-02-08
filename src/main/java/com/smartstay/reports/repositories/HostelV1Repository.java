package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.HostelV1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostelV1Repository extends JpaRepository<HostelV1, String> {
    HostelV1 findByHostelId(String hostelId);
}
