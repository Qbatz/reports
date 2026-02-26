package com.smartstay.reports.service;

import com.smartstay.reports.dao.Rooms;
import com.smartstay.reports.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomsService {
    @Autowired
    private RoomRepository roomRepository;

    public List<Rooms> getRoomsInfo(List<Integer> listRooms) {
        return roomRepository.findAllById(listRooms);
    }
}
