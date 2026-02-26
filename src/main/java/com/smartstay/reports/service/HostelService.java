package com.smartstay.reports.service;

import com.smartstay.reports.dao.HostelV1;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.repositories.HostelV1Repository;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.utils.HostelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostelService {
    @Autowired
    private HostelV1Repository hostelV1Repository;

    public HostelInfo hostelInfo(String hostelId) {
        HostelV1 hostelV1 = hostelV1Repository.findByHostelId(hostelId);
        if (hostelV1 == null) {
            return null;
        }

        StringBuilder hostelFullAddress = new StringBuilder();
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getHouseNo());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getStreet() != null && !hostelV1.getStreet().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getStreet());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getCity() != null && !hostelV1.getCity().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getCity());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getState() != null && !hostelV1.getState().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getState());
            hostelFullAddress.append("-");
        }
        if (hostelV1.getPincode() != 0) {
            hostelFullAddress.append(hostelV1.getPincode());
        }
        return new HostelInfo(hostelV1.getHostelName(), hostelFullAddress.toString());
    }

    public HostelInformation getHostelInformation(String hostelId) {
        HostelV1 hostelV1 = hostelV1Repository.findByHostelId(hostelId);
        if (hostelV1 != null) {


            return new HostelInformation(hostelV1.getHostelName(),
                    HostelUtils.getHostelAddress(hostelV1),
                    hostelV1.getMobile(),
                    hostelV1.getCity(),
                    hostelV1.getState(),
                    hostelV1.getPincode(),
                    hostelV1.getHouseNo(),
                    hostelV1.getStreet());
        }

        return null;
    }
}
