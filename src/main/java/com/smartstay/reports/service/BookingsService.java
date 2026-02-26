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
}
