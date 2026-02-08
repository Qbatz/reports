package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorsRepository extends JpaRepository<Floors, Integer> {
}
