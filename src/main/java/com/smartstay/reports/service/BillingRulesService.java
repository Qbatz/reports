package com.smartstay.reports.service;

import com.smartstay.reports.dao.BillingRules;
import com.smartstay.reports.dto.billingRules.BillingDates;
import com.smartstay.reports.ennum.PeriodEnum;
import com.smartstay.reports.exceptions.BadRequestException;
import com.smartstay.reports.repositories.BillingRulesRepository;
import com.smartstay.reports.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class BillingRulesService {

    @Autowired
    private BillingRulesRepository billingRulesRepository;
    
    public BillingRules getLatestBillingRulesByHostelId(String hostelId){
        return billingRulesRepository.findLatestByHostelId(hostelId);
    }

    public BillingDates computeBillingDates(BillingRules billingRules, Date requestedDate) {

        if (requestedDate == null){
            throw new BadRequestException("Date is null");
        }

        requestedDate = DateUtils.getStartOfDay(requestedDate);

        int billStartDate = billingRules != null ? billingRules.getBillingStartDate() : 1;
        int billingRuleDueDate = billingRules != null ? billingRules.getBillDueDays() : 10;

        boolean hasGracePeriod = false;
        int gracePeriodDays = 0;
        String typeOfBilling = null;
        String billingModel = null;
        if (billingRules != null){
            hasGracePeriod = billingRules.isHasGracePeriod();
            gracePeriodDays = billingRules.getGracePeriodDays() != null ? billingRules.getGracePeriodDays() : 0;
            typeOfBilling = billingRules.getTypeOfBilling();
            billingModel = billingRules.getBillingModel();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedDate);

        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        if (DateUtils.compareWithTwoDates(requestedDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = DateUtils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);
        Date endDate = DateUtils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(calendar.getTime(), endDate, dueDate, billingRuleDueDate,
                hasGracePeriod, gracePeriodDays, typeOfBilling, billingModel);
    }

//    public BillingDates getBillingDatesByPeriod(String period, BillingRules billingRules) {
//
//        PeriodEnum periodEnum;
//        try {
//            periodEnum = PeriodEnum.valueOf(period);
//        } catch (Exception e) {
//            throw new BadRequestException("Invalid period");
//        }
//
//        if (billingRules == null){
//            throw new BadRequestException("Billing rules not found");
//        }
//
//        Date today = new Date();
//
//        BillingDates billingDates = computeBillingDates(billingRules, today);
//
//        if (PeriodEnum.THIS_MONTH.equals(periodEnum)) {
//            return billingDates;
//        } else if (PeriodEnum.LAST_MONTH.equals(periodEnum)) {
//            Date lastMonthDate = DateUtils.getLastMonthDateByNoOfMonths(today, 1);
//            return computeBillingDates(billingRules, lastMonthDate);
//        } else if (PeriodEnum.LAST_3_MONTHS.equals(periodEnum)) {
//            Date last3MonthsDate = DateUtils.getLastMonthDateByNoOfMonths(today, 3);
//            return computeBillingDates(billingRules, last3MonthsDate);
//        } else if (PeriodEnum.LAST_6_MONTHS.equals(periodEnum)) {
//            Date last6MonthsDate = DateUtils.getLastMonthDateByNoOfMonths(today, 6);
//            return computeBillingDates(billingRules, last6MonthsDate);
//        }
//
//        return billingDates;
//    }

    public BillingDates getBillingDatesByPeriod(String period, BillingRules billingRules) {

        PeriodEnum periodEnum;
        try {
            periodEnum = PeriodEnum.valueOf(period);
        } catch (Exception e) {
            throw new BadRequestException("Invalid period");
        }

        if (billingRules == null) {
            throw new BadRequestException("Billing rules not found");
        }

        Date today = DateUtils.getStartOfDay(new Date());

        BillingDates currentBillingDates = computeBillingDates(billingRules, today);

        int monthsToSubtract = switch (periodEnum) {
            case THIS_MONTH -> 0;
            case LAST_MONTH -> 1;
            case LAST_3_MONTHS -> 2;
            case LAST_6_MONTHS -> 5;
            default -> 0;
        };

        if (monthsToSubtract == 0) {
            return currentBillingDates;
        }

        Date pastDate = DateUtils.getLastMonthDateByNoOfMonths(today, monthsToSubtract);

        BillingDates pastBillingDates = computeBillingDates(billingRules, pastDate);

        return new BillingDates(
                pastBillingDates.currentBillStartDate(),
                currentBillingDates.currentBillEndDate(),
                currentBillingDates.dueDate(),
                currentBillingDates.dueDays(),
                currentBillingDates.hasGracePeriod(),
                currentBillingDates.gracePeriodDays(),
                currentBillingDates.typeOfBilling(),
                currentBillingDates.billingModel()
        );
    }
}
