package com.smartstay.reports.services;

import com.smartstay.reports.dao.HostelV1;
import com.smartstay.reports.dao.OrderHistory;
import com.smartstay.reports.dao.Plans;
import com.smartstay.reports.dao.Subscription;
import com.smartstay.reports.repositories.HostelV1Repository;
import com.smartstay.reports.repositories.OrderHistoryRepository;
import com.smartstay.reports.repositories.PlansRepository;
import com.smartstay.reports.repositories.SubscriptionRepository;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.HostelInfoInvoice;
import com.smartstay.reports.responses.subscription.BankInfo;
import com.smartstay.reports.responses.subscription.OrderInfo;
import com.smartstay.reports.responses.subscription.SubscriptionInfo;
import com.smartstay.reports.responses.subscription.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubscriptionsService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private PlansRepository plansRepository;

    @Autowired
    private HostelV1Repository hostelV1Repository;

    public ResponseEntity<?> getSubscriptionDetails(String hostelId, String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId);
        if (subscription==null) {
            return ResponseEntity.notFound().build();
        }
//        Subscription subscription = subscriptionOpt.get();
//        System.out.println("Subscriptions");

//        Optional<OrderHistory> orderHistoryOpt = orderHistoryRepository.findBySubscriptionId(subscriptionId);
//        if (orderHistoryOpt.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        OrderHistory orderHistory = orderHistoryOpt.get();

        Plans plans = plansRepository.findPlanByPlanCode(subscription.getPlanCode());
        if (plans==null) {
            return ResponseEntity.notFound().build();
        }
//        Plans plans = plansOpt.get();

        HostelV1 hostel = hostelV1Repository.findByHostelId(hostelId);
        if (hostel==null) {
            return ResponseEntity.notFound().build();
        }
//        HostelV1 hostel = hostelOpt.get();

//        String address = String.format("%s, %s, %s, %s, %s, %d",
//                hostel.getHouseNo() != null ? hostel.getHouseNo() : "",
//                hostel.getStreet() != null ? hostel.getStreet() : "",
//                hostel.getLandmark() != null ? hostel.getLandmark() : "",
//                hostel.getCity() != null ? hostel.getCity() : "",
//                hostel.getState() != null ? hostel.getState() : "",
//                hostel.getPincode());

        HostelInfoInvoice hostelInfo = new HostelInfoInvoice(
                hostel.getHostelName() != null ? hostel.getHostelName() : "",
                hostel.getMainImage() != null ? hostel.getMainImage() : "",
                hostel.getHouseNo() != null ? hostel.getHouseNo() : "",
                hostel.getLandmark() != null ? hostel.getLandmark() : "",
                hostel.getStreet() != null ? hostel.getStreet() : "",
                hostel.getCity() != null ? hostel.getCity() : "",
                hostel.getState() != null ? hostel.getState() : "",
                hostel.getEmailId() != null ? hostel.getEmailId() : "",
                String.valueOf(hostel.getPincode()),
                hostel.getMobile() != null ? hostel.getMobile() : ""
        );

        double price = plans.getPrice() != null ? plans.getPrice() : 0.0;
        double cgst = plans.getCgst() != null ? plans.getCgst() : 0;
        double sgst = plans.getSgst() != null ? plans.getSgst() : 0;
        double discountAmount = subscription.getDiscountAmount() != null ? subscription.getDiscountAmount() : 0.0;
        double finalPrice = plans.getFinalPrice() != null ? plans.getFinalPrice() : 0.0;

        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(
                subscription.getPlanName() != null ? subscription.getPlanName() : "",
                plans.getPlanType() != null ? plans.getPlanType() : "",
                subscription.getPlanCode() != null ? subscription.getPlanCode() : "",
                price,
                price,
                discountAmount,
                cgst,
                sgst,
                (price * cgst / 100),
                (price * sgst / 100),
                finalPrice
        );

        OrderInfo orderInfo = new OrderInfo(
                subscription.getSubscriptionNumber() != null ? subscription.getSubscriptionNumber() : "",
                subscription.getCreatedAt() != null ? subscription.getCreatedAt().toString() : "",
                "Due on Receipt",
                subscription.getCreatedAt() != null ? subscription.getCreatedAt().toString() : ""
        );

        BankInfo bankInfo = new BankInfo(
                "S3 Remotica Technologies",
                "120026855214",
                "CNRB0001130",
                "Canara bank",
                "VK Pudur"
        );

        SubscriptionResponse response = new SubscriptionResponse(
                "", // companyLogo
                "S3 Remotica Technologies",
                "S3",
                "7/96,North Street, Athisayapuram Tenkasi Tamil Nadu 627861 India",
                "33AEXFS4390A1ZT",
                "support@s3remotica.com",
                "", // signature
                "Tamil Nadu (33)",
                "Thank you for your business",
                "Payment is due within 30 days of the invoice date.",
                hostelInfo,
                subscriptionInfo,
                orderInfo,
                bankInfo
        );

        return ResponseEntity.ok(response);
    }
}
