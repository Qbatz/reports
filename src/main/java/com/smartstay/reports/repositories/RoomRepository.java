package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Integer> {

    List<Rooms> findByHostelIdAndSharingTypeIn(String hostelId, List<Integer> shareType);

    List<Rooms> findAllByFloorIdInAndIsActiveTrueAndIsDeletedFalse(Set<Integer> floorIds);
}
