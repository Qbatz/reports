package com.smartstay.reports.responses.invoice;

import java.util.List;

public record InvRoomRes(Integer roomId,
                         String roomName,
                         Integer floorId,
                         String floorName,
                         List<InvBedRes> beds) {
}
