package com.smartstay.reports.wrappers;

import com.smartstay.reports.dao.BookingsV1;
import com.smartstay.reports.dao.Customers;
import com.smartstay.reports.dao.Rooms;
import com.smartstay.reports.dto.beds.BedInformations;
import com.smartstay.reports.dto.customer.CustomerInfo;
import com.smartstay.reports.dto.customer.StayInfo;
import com.smartstay.reports.ennum.BookingStatus;
import com.smartstay.reports.ennum.CustomerStatus;
import com.smartstay.reports.utils.NameUtils;
import com.smartstay.reports.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class CustomersMapper implements Function<Customers, com.smartstay.reports.dto.customer.Customers> {

    List<BookingsV1> listBooking = null;
    List<BedInformations> listBedInformations = null;

    public CustomersMapper(List<BookingsV1> bookings, List<BedInformations> listBedInformations) {
        this.listBooking = bookings;
        this.listBedInformations = listBedInformations;
    }

    @Override
    public com.smartstay.reports.dto.customer.Customers apply(Customers customers) {

        String checkInDate = null;
        String bookingDate = null;
        String checkoutDate = null;

        String roomName = null;
        String floorName = null;
        String bedName = null;
        String sharingType = null;
        String stayDuration = null;

        String fullName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        String initils = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());

        if (listBooking != null) {
            BookingsV1 bookingsV1 = listBooking
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                    .findFirst()
                    .orElse(null);

            if (bookingsV1 != null) {
                if (bookingsV1.getJoiningDate() != null) {
                    checkInDate = Utils.dateToString(bookingsV1.getJoiningDate());
                }
                else if (bookingsV1.getExpectedJoiningDate() != null) {
                    bookingDate = Utils.dateToString(bookingsV1.getBookingDate());
                }

                if (!bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())
                && !bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
                    if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()) ||
                            bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())) {
                        long duration = Utils.findNumberOfDays(bookingsV1.getJoiningDate(), new Date());
                        stayDuration = String.valueOf(duration);
                    }
                    else if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
                        long duration = Utils.findNumberOfDays(bookingsV1.getJoiningDate(), bookingsV1.getCheckoutDate());
                        stayDuration = String.valueOf(duration) +" days";
                    }

                }

                if (listBedInformations != null) {
                    BedInformations bedInformations = listBedInformations
                            .stream()
                            .filter(i -> i.bedId().equals(bookingsV1.getBedId()))
                            .findFirst()
                            .orElse(null);

                    if (bedInformations != null) {
                        bedName = bedInformations.bedName();
                        floorName = bedInformations.floorName();
                        roomName = bedInformations.roomName();

                        if (bedInformations.sharing() == 1) {
                            sharingType = "Single sharing";
                        }
                        else if (bedInformations.sharing() == 2) {
                            sharingType = "Two sharing";
                        }
                        else if (bedInformations.sharing() == 3) {
                            sharingType = "Three sharing";
                        }
                        else {
                            sharingType = bedInformations.sharing() + " sharing";
                        }
                    }
                }

             }
        }



        CustomerInfo customerInfo = new CustomerInfo(customers.getFirstName(),
                customers.getLastName(),
                fullName,
                customers.getProfilePic(),
                initils,
                customers.getMobile());
        StayInfo stayInfo = new StayInfo(bedName,
                floorName,
                roomName,
                sharingType,
                checkInDate,
                checkoutDate,
                stayDuration);

        return new com.smartstay.reports.dto.customer.Customers(customerInfo, stayInfo);
    }
}
