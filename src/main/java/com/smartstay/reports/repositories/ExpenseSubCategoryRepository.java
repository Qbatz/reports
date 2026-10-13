package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.ExpenseSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseSubCategoryRepository extends JpaRepository<ExpenseSubCategory, Long> {
}
