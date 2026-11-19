package com.smartstay.reports.dao;

import com.smartstay.reports.converters.IntegerListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer billingStartDate;
    private Integer billDueDays;
    private Integer noticePeriod;
    private boolean isInitial;
    private boolean hasGracePeriod;
    private Integer gracePeriodDays;
    //From billing type enum
    private String typeOfBilling;
    //Billing model enum
    private String billingModel;

    @Convert(converter = IntegerListConverter.class)
    private List<Integer> reminderDays;

    private boolean shouldNotify;
    private Date startFrom;
    private Date endTill;
    private Date createdAt;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    private HostelV1 hostel;
}
