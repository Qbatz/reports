package com.smartstay.reports.service;

import com.smartstay.reports.dao.BookingsV1;
import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.dao.Rooms;
import com.smartstay.reports.dao.TransactionV1;
import com.smartstay.reports.dto.beds.BedInformations;
import com.smartstay.reports.dto.customer.CustomerInfo;
import com.smartstay.reports.dto.customer.CustomersDetails;
import com.smartstay.reports.dto.customer.HeaderInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.ennum.BookingStatus;
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
    @Autowired
    private HostelService hostelService;
    @Autowired
    private TransactionV1Service transactionV1Service;

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
        List<TransactionV1> listTransactions = transactionV1Service.findTransactions(hostelId, customerIds);

        List<Integer> bedIds = listBookings
                .stream()
                .map(BookingsV1::getBedId)
                .toList();

        List<BedInformations> listBedInformations = bedsService.getBedInformations(bedIds);
        List<com.smartstay.reports.dto.customer.Customers> listCustomerInfo = listCustomers
                .stream()
                .map(i -> new CustomersMapper(listBookings, listBedInformations, listTransactions).apply(i))
                .toList();
        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);

        String totalTenants = String.valueOf(listCustomers.size());
        long activeCounts = 0;
        long noticePeriodCount = 0;
        long checkoutCount = 0;
        long inactiveCount = 0;
        long bookingCount = 0;

        activeCounts = listBookings.stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()))
                .count();

        noticePeriodCount = listBookings
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name()))
                .count();

        checkoutCount = listBookings
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name()) ||
                        i.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name()))
                .count();

        inactiveCount = listBookings
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name()))
                .count();

        bookingCount = listBookings
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name()))
                .count();
        HeaderInfo headerInfo = new HeaderInfo(totalTenants,
                String.valueOf(activeCounts),
                String.valueOf(noticePeriodCount),
                String.valueOf(checkoutCount),
                String.valueOf(inactiveCount),
                String.valueOf(bookingCount));

        CustomersDetails details = new CustomersDetails(hostelInformation, headerInfo, listCustomerInfo);
        return details;
    }

}
