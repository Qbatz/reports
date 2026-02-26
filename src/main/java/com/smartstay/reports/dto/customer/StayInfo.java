package com.smartstay.reports.dto.customer;

public record StayInfo(String bedName,
                       String floorName,
                       String roomName,
                       String sharingType,
                       String checkInDate,
                       String checkoutDate,
                       String stayDuration) {
}
