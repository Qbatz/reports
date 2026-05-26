package com.smartstay.reports.responses.subscription;

import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.HostelInfoInvoice;

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
        HostelInfoInvoice hostelInfo,
        SubscriptionInfo subscriptionInfo,
        OrderInfo orderInfo,
        BankInfo bankInfo
) {
}
