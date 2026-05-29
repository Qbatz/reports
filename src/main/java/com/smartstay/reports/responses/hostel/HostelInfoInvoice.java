package com.smartstay.reports.responses.hostel;

public record HostelInfoInvoice(
        String hostelName,
        String hostelImage,
        String houseNo,
        String landmark,
        String street,
        String city,
        String state,
        String emailId,
        String pincode,
        String mobile
) {
}
