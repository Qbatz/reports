package com.smartstay.reports.service;

import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.repositories.CustomersRepository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServices {
    @Autowired
    private CustomersRepository customersRepository;

    public CustomerInfo getCustomerInfo(String customerId) {
        Customers customers = customersRepository.findByCustomerId(customerId);
        if (customers == null) {
            return null;
        }

        StringBuilder fullAddress = new StringBuilder();
        StringBuilder fullName = new StringBuilder();

        if (customers.getFirstName() != null) {
            fullName.append(customers.getFirstName());
        }
        if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(customers.getLastName());
        }

        if (customers.getHouseNo() != null) {
            fullAddress.append(customers.getHouseNo());
            fullAddress.append(", ");
        }
        if (customers.getStreet() != null) {
            fullAddress.append(customers.getStreet());
            fullAddress.append(", ");
        }
        if (customers.getCity() != null) {
            fullAddress.append(customers.getCity());
            fullAddress.append(", ");
        }
        if (customers.getState() != null) {
            fullAddress.append(customers.getState());
            fullAddress.append("-");
        }

        if (customers.getPincode() != 0) {
            fullAddress.append(customers.getPincode());
        }

        return new CustomerInfo(fullName.toString(),
                "+91-" + customers.getMobile(),
                fullAddress.toString(),
                Utils.dateToString(customers.getJoiningDate()));
    }
}
