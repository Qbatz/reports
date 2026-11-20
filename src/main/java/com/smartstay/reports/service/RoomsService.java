package com.smartstay.reports.service;

import com.smartstay.reports.dao.Rooms;
import com.smartstay.reports.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoomsService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Rooms> findByHostelIdAndShareType(String hostelId, List<Integer> shareTypes) {
        return roomRepository.findByHostelIdAndSharingTypeIn(hostelId, shareTypes);
    }

    public List<Rooms> getAllByFloorIds(Set<Integer> floorIds) {
        return roomRepository.findAllByFloorIdInAndIsActiveTrueAndIsDeletedFalse(floorIds);
    }
}
