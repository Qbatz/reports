package com.smartstay.reports.utils;

import com.smartstay.reports.dao.HostelV1;

public class HostelUtils {

    public static String getHostelAddress(HostelV1 hostelV1) {
        StringBuilder address = new StringBuilder();
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            address.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getStreet() != null && !hostelV1.getStreet().trim().equalsIgnoreCase("")) {
            if (!address.toString().isEmpty()) {
                address.append(", ");
            }
            address.append(hostelV1.getStreet());
        }
        if (hostelV1.getCity() != null && !hostelV1.getCity().trim().equalsIgnoreCase("")) {
            if (!address.toString().isEmpty()) {
                address.append(", ");
            }
            address.append(hostelV1.getCity());
        }
        if (hostelV1.getState() != null && !hostelV1.getState().trim().equalsIgnoreCase("")) {
            if (!address.toString().isEmpty()) {
                address.append(", ");
            }
            address.append(hostelV1.getState());
        }
        if (hostelV1.getPincode() != 0) {
            if (!address.toString().isEmpty()) {
                address.append(", ");
            }
            address.append(hostelV1.getPincode());
        }

        return address.toString();

    }
}
