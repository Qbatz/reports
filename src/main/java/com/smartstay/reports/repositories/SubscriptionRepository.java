package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Subscription findBySubscriptionId(String subscriptionId);
}
