package com.smartstay.reports.responses.invoice;

import java.util.List;

public record InvConsolidatedFiltersRes(List<UserFilterRes> createdBy,
                                        List<EnumFilterRes> period,
                                        List<EnumFilterRes> paymentStatus,
                                        List<EnumFilterRes> invoiceMode,
                                        List<EnumFilterRes> invoiceType,
                                        List<FloorsFilterRes> floors,
                                        List<EnumFilterRes> groupBy) {
}
