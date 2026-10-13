package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, String> {
//    Optional<OrderHistory> findBySubscriptionId(String subscriptionId);
}
