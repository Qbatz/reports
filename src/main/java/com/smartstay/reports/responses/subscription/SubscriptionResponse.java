package com.smartstay.reports.responses.subscription;

import com.smartstay.reports.responses.hostel.HostelInfo;

public record SubscriptionResponse(
        String companyLogo,
        String companyName,
        String companyId,
        String companyAddress,
        String companyGSTIN,
        String supportEmail,
        String signature,
        String placeOfSupply,
        String notes,
        String tandc,
        HostelInfo hostelInfo,
        SubscriptionInfo subscriptionInfo,
        OrderInfo orderInfo,
        BankInfo bankInfo
) {
}
