package com.smartstay.reports.service;

import com.smartstay.reports.dao.BookingsV1;
import com.smartstay.reports.repositories.BookingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BookingsService {

    @Autowired
    private BookingsRepository bookingsRepository;
    public List<BookingsV1> findBookingsByHostelIdAndStartDateAndEndDate(String hostelId, Date sDate, Date eDate) {
        List<BookingsV1> listBookings = bookingsRepository.findAllBookingsWithFilters(hostelId, sDate, eDate);
        if (listBookings == null) {
            listBookings = new ArrayList<>();
        }
        return listBookings;
    }

    public BookingsV1 findByCustomerId(String customerId, String hostelId) {
        return bookingsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
    }

    public List<BookingsV1> findAllBookingsWithFilters(String hostelId, Date startDate, Date endDate, List<String> customerIds, List<String> status, List<Integer> roomIds, List<Integer> floorIds) {
        return bookingsRepository.findAllBookingsWithFilters(hostelId, startDate, endDate, (customerIds != null && !customerIds.isEmpty()) ? customerIds : null, (status != null && !status.isEmpty()) ? status : null, (roomIds != null && !roomIds.isEmpty()) ? roomIds : null, (floorIds != null && !floorIds.isEmpty()) ? floorIds : null);
    }
}
