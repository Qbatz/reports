package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositories extends JpaRepository<Users, String> {
}
