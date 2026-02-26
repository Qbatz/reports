package com.smartstay.reports.service;

import com.smartstay.reports.dao.BookingsV1;
import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.dao.Rooms;
import com.smartstay.reports.dto.beds.BedInformations;
import com.smartstay.reports.dto.customer.CustomerInfo;
import com.smartstay.reports.dto.customer.CustomersDetails;
import com.smartstay.reports.repositories.CustomersRepository;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.wrappers.CustomersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.List;

@Service
public class CustomersService {
    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private PDFServices pdfServices;

    public ResponseEntity<?> getCustomers(String hostelId, String startDate, String endDate) {
        CustomersDetails details = getCustomerDetails(hostelId, startDate, endDate);
        Context context = new Context();
        context.setVariable("tenants", details);

        String receiptsUrl = pdfServices.generateReceiptPdf("tenant", context);
        return new ResponseEntity<>(receiptsUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getCustomersDetails(String hostelId, String startDate, String endDate) {
        CustomersDetails details = getCustomerDetails(hostelId, startDate, endDate);
        return new ResponseEntity<>(details, HttpStatus.OK);

    }


    public CustomersDetails getCustomerDetails(String hostelId, String startDate, String endDate) {
        Date sDate = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        List<BookingsV1> listBookings = bookingsService.findBookingsByHostelIdAndStartDateAndEndDate(hostelId, sDate, eDate);

        List<String> customerIds = listBookings
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();
        List<Customers> listCustomers = customersRepository.findAllById(customerIds);

        List<Integer> bedIds = listBookings
                .stream()
                .map(BookingsV1::getBedId)
                .toList();

        List<BedInformations> listBedInformations = bedsService.getBedInformations(bedIds);
        List<com.smartstay.reports.dto.customer.Customers> listCustomerInfo = listCustomers
                .stream()
                .map(i -> new CustomersMapper(listBookings, listBedInformations).apply(i))
                .toList();

        CustomersDetails details = new CustomersDetails(listCustomerInfo);
        return details;
    }

}
