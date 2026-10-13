package com.smartstay.reports.service;

import com.smartstay.reports.dao.VendorV1;
import com.smartstay.reports.repositories.VendorV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VendorService {
    @Autowired
    private VendorV1Repository vendorV1Repository;
    public List<VendorV1> getVendorsByVendorIds(List<Integer> vendorExpenseIds) {
        List<VendorV1> listVendors = vendorV1Repository.findByVendorIds(vendorExpenseIds);
        if (listVendors == null) {
            return new ArrayList<>();
        }
        return listVendors;
    }
}
