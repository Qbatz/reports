package com.smartstay.reports.service;

import com.smartstay.reports.dto.beds.BedDetails;
import com.smartstay.reports.repositories.BedsRepository;
import com.smartstay.reports.responses.invoice.BedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BedsService {
    @Autowired
    private BedsRepository bedsRepository;

    public BedInfo getBedDetails(Integer bedId) {
        BedDetails bedDetails = bedsRepository.findByBedId(bedId);
        if (bedDetails == null) {
            return null;
        }
        StringBuilder bd = new StringBuilder();
        bd.append(bedDetails.getFloorName());
        bd.append(",");
        bd.append(bedDetails.getRoomName());
        bd.append("-");
        bd.append(bedDetails.getBedName());
        return new BedInfo(bd.toString());
    }
}
