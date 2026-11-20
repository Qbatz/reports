package com.smartstay.reports.responses.invoice;

import java.util.List;

public record InvFloorsRes(Integer floorId,
                           String floorName,
                           List<InvRoomRes> rooms) {
}
