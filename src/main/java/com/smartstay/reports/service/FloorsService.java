package com.smartstay.reports.service;

import com.smartstay.reports.dao.Floors;
import com.smartstay.reports.repositories.FloorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class FloorsService {

    @Autowired
    private FloorsRepository floorsRepository;

    public List<Floors> getByFloorIds(Set<Integer> floorIds) {
        return floorsRepository.findAllByFloorIdInAndIsActiveTrueAndIsDeletedFalse(floorIds);
    }

    public List<Floors> getAllByHostelId(String hostelId) {
        return floorsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }
}
