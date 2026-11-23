package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.FilterOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterOptionsRepository extends JpaRepository<FilterOptions, Long> {

    FilterOptions findByModuleName(String moduleName);
}
