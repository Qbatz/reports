package com.smartstay.reports.dto.beds;

public record BedInformations(String bedName,
                              String roomName,
                              String floorName,
                              Integer sharing,
                              Integer bedId) {
}
