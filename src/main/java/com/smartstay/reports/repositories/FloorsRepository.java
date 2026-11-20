package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FloorsRepository extends JpaRepository<Floors, Integer> {

    List<Floors> findAllByFloorIdInAndIsActiveTrueAndIsDeletedFalse(Set<Integer> floorIds);

    List<Floors> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);
}
