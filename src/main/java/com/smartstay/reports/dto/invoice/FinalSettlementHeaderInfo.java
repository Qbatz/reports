package com.smartstay.reports.dto.invoice;

public record FinalSettlementHeaderInfo(String invoiceNo,
                                        String houseNo,
                                        String street,
                                        String city,
                                        String state,
                                        Integer pincode,
                                        String gstNumber,
                                        String phoneNumber,
                                        String countryCode,
                                        String hostelImage,
                                        String emailId) {
}
