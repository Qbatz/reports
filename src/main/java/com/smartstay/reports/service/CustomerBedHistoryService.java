package com.smartstay.reports.service;

import com.smartstay.reports.dao.CustomersBedHistory;
import com.smartstay.reports.repositories.CustomerBedHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerBedHistoryService {
    @Autowired
    private CustomerBedHistoryRepository customerBedHistoryRepository;


    public CustomersBedHistory getCustomerBedByStartDate(String customerId, Date startDate, Date endDate) {
        CustomersBedHistory cbh = customerBedHistoryRepository.findByCustomerIdAndDate(customerId, startDate, endDate);
        return cbh;
    }
}
