package com.smartstay.reports.service;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.dto.beds.BedDetails;
import com.smartstay.reports.dto.customer.Deductions;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.dto.customer.StayInfo;
import com.smartstay.reports.dto.invoice.*;
import com.smartstay.reports.dto.settlement.FinalSettlementInvoiceInfo;
import com.smartstay.reports.dto.settlement.RentInfo;
import com.smartstay.reports.dto.settlement.WalltetItems;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.ennum.PaymentStatus;
import com.smartstay.reports.repositories.InvoicesV1Repository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.InvoiceHeader;
import com.smartstay.reports.responses.hostel.ListInvoiceItems;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.invoice.*;
import com.smartstay.reports.responses.invoice.InvoiceItems;
import com.smartstay.reports.services.InvoiceRedemptionService;
import com.smartstay.reports.services.SettlementItemService;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.wrappers.CurrentRentBreakUp;
import com.smartstay.reports.wrappers.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartstay.reports.ennum.PaymentStatus.*;

@Service
public class InvoiceService {
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private PDFServices invoicePDFServices;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomerServices customerServices;
    @Autowired
    private CustomerBedHistoryService customerBedHistoryService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private InvoiceDiscountService invoiceDiscountService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private PDFServices pdfServices;
    @Autowired
    private SettlementItemService settlementItemService;
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;

    public ResponseEntity<?> getInvoiceReport(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            SettlementItems settlementItems = settlementItemService.getSettlementItems(invoiceId);
            if (settlementItems != null) {
                return getInvoiceReportNew(invoicesV1.getHostelId(), invoiceId);
            }
        }
        InvoiceInfo invoiceInfo = getInvoiceInfo(invoicesV1);
        Context context = new Context();
        context.setVariable("invoice", invoiceInfo);

        String invoiceUrl = invoicePDFServices.generatePdf(invoiceId, "invoice", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceDetails(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        InvoiceInfo invoiceInfo = getInvoiceInfo(invoicesV1);

        return new ResponseEntity<>(invoiceInfo, HttpStatus.OK);
    }

    public InvoiceInfo getInvoiceInfo(InvoicesV1 invoicesV1) {
        double paidAmount = 0.0;
        double discount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }
        double balanceAmount = calculateBalance(invoicesV1.getTotalAmount(), paidAmount, invoicesV1.getPaymentStatus());
        double totalDeductionAmount = 0.0;
        List<InvoiceItems> invoiceItems = new ArrayList<>();
        List<Deductions> listDeductions = new ArrayList<>();

        String invoiceDate = Utils.dateToString(invoicesV1.getInvoiceStartDate());
        if (invoicesV1.getInvoiceDate() != null) {
            invoiceDate = Utils.dateToString(invoicesV1.getInvoiceDate());
        }
        String rentalPeriod = Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate()) + "-" + Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate());

        discount = invoiceDiscountService.getInoiceDiscount(invoicesV1.getHostelId(), invoicesV1.getInvoiceId());

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {

            totalDeductionAmount = invoicesV1.getInvoiceItems()
                    .stream()
                    .mapToDouble(com.smartstay.reports.dao.InvoiceItems::getAmount)
                    .sum();
            listDeductions = invoicesV1.getInvoiceItems().stream().map(i -> {
                Deductions d = new Deductions();
                if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                    if (i.getOtherItem() != null) {
                        i.setInvoiceItem(i.getOtherItem());
                    }
                } else {
                    i.setInvoiceItem(i.getInvoiceItem());
                }
                d.setType(i.getInvoiceItem());
                d.setAmount(i.getAmount());

                return d;
            }).toList();

            invoiceItems.add(new InvoiceItems(InvoiceType.SETTLEMENT.name(), String.valueOf(invoicesV1.getBasePrice()), invoicesV1.getInvoiceNumber()));
        }
        else {
            invoiceItems = invoicesV1
                    .getInvoiceItems()
                    .stream()
                    .map(i -> {
                        String item = null;
                        String amount = null;
                        if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.RENT.name())) {
                            item = "Rent";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.EB.name())) {
                            item = "Electricity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.AMENITY.name())) {
                            item = "Amenity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.BOOKING.name())) {
                            item = "Security Deposit (Advance)";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.ADVANCE.name())) {
                            item = "Security Deposit (Advance)";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.MAINTENANCE.name())) {
                            item = "Maintenance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                            item = i.getOtherItem();
                        }

                        amount = String.valueOf(Math.round(i.getAmount()));

                        return new InvoiceItems(item, amount, invoicesV1.getInvoiceNumber());
                    })
                    .toList();
        }

        String invoiceType = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Payment Bill";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            rentalPeriod = "";
            invoiceType = "Security Deposit(Booking)";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Security Deposit";
            rentalPeriod = "";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";

        }


        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        CustomersBedHistory cbh = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            cbh = customerBedHistoryService.getBookedBed(invoicesV1.getCustomerId());
        }
        else {
            cbh = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        }
        BedInfo bedInfo = null;
        if (cbh != null) {
            bedInfo = bedsService.getBedDetails(cbh.getBedId());
        }

        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());


        InvoiceInfo invoiceInfo = new InvoiceInfo(
                invoicesV1.getInvoiceNumber(),
                invoiceDate,
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                rentalPeriod,
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(totalDeductionAmount)),
                String.valueOf(Math.round(paidAmount)),
                String.valueOf(Math.round(balanceAmount)),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(discount)),
                invoiceType,
                invoiceItems,
                listDeductions,
                hostelInfo,
                customerInfo,
                bedInfo,
                templateInfo
        );

        return invoiceInfo;
    }

    private FinalSettlementInfo newSettlementInvoice(InvoicesV1 invoicesV1) {
        HostelV1 hostelV1 = hostelService.getHostel(invoicesV1.getHostelId());
        SettlementItems settlementItems = settlementItemService.getSettlementItems(invoicesV1.getInvoiceId());

        double subTotal = 0.0;
        double deductionAmount = 0.0;
        double unpaidInvoiceAmount = 0.0;
        double electricityAmount = 0.0;
        double finalAmount = 0.0;

        double currentPayablemount = 0.0;
        double currentPaidAmount = 0.0;
        double currentMonthOtherAmounts = 0.0;
        double currentMonthTotalAmount = 0.0;
        double stayDays = 0.0;
        double currentMonthPayableRent = 0.0;
        UnpaidInvoiceInfo unpaidInvoiceInfo = null;
        if (settlementItems.getUnpaidInvoices() != null) {
            if (!settlementItems.getUnpaidInvoices().isEmpty()) {
                int noOfInvoiceInfo = settlementItems.getUnpaidInvoices().size();
                double unpaidInvoiceTotalAmount = settlementItems
                        .getUnpaidInvoices()
                        .stream()
                        .mapToDouble(i -> {
                            if (i.getPendingAmount() != null) {
                                return i.getPendingAmount();
                            }
                            return 0.0;
                        })
                        .sum();
                unpaidInvoiceAmount = unpaidInvoiceTotalAmount;
                List<UnpaidInvoiceItem> listUnpaidInvoiceItems = settlementItems
                        .getUnpaidInvoices()
                        .stream()
                        .map(i -> {
                            double totalAmount = i.getInvoiceAmount();
                            double paidAmount = i.getInvoiceAmount() - i.getPendingAmount();
                            return new UnpaidInvoiceItem(i.getInvoiceNo(), i.getInvoiceAmount(), paidAmount, i.getInvoiceAmount());
                        })
                        .toList();
                unpaidInvoiceInfo = new UnpaidInvoiceInfo(noOfInvoiceInfo,
                        unpaidInvoiceTotalAmount,
                        listUnpaidInvoiceItems);
            }
        }

        DeductionsInfo deductionsInfo = null;
        if (invoicesV1.getDeductions() != null) {
            if (!invoicesV1.getDeductions().isEmpty()) {
                double totalDeductionAmount = invoicesV1.getDeductions()
                        .stream()
                        .mapToDouble(i -> {
                            if (i.getAmount() != null) {
                                return i.getAmount();
                            }
                            return 0.0;
                        })
                        .sum();

                double paidAmount = invoicesV1
                        .getDeductions()
                        .stream()
                        .mapToDouble(i -> {
                            if (i.getPaidAmount() != null) {
                                return i.getPaidAmount();
                            }
                            return 0.0;
                        })
                        .sum();
                double pendingAmount = totalDeductionAmount - paidAmount;
                deductionAmount = pendingAmount;
                List<DeductionsItem> listDeductionItems = invoicesV1
                        .getDeductions()
                        .stream()
                        .map(i -> {
                            double pAmount = 0.0;
                            if (i.getPaidAmount() != null) {
                                pAmount = i.getAmount() - i.getPaidAmount();
                            }
                            else {
                                pAmount = i.getAmount();
                            }
                            return new DeductionsItem(i.getType(), i.getPaidAmount(), i.getAmount(), pAmount);
                        })
                        .toList();

                deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidAmount,
                        pendingAmount,
                        listDeductionItems);
            }
        }

        AdvanceItems advanceItems = getRedeemedListFromAdvance(invoicesV1.getHostelId(), invoicesV1.getCustomerId());
        AdvanceItems bookingItems = getRedeemedListFromBookings(invoicesV1.getHostelId(), invoicesV1.getCustomerId());

        if (advanceItems != null) {
            if (advanceItems.availableAdvanceBalance() != null) {
//                subTotal = subTotal - advanceItems.availableAdvanceBalance();
            }
        }
        if (bookingItems != null) {
            if (bookingItems.availableAdvanceBalance() != null) {
//                subTotal = subTotal - bookingItems.availableAdvanceBalance();
            }
        }
        RentInfo rentInfo = null;
        WalletInfo walletInfo = null;
        CurrentRentInfo currentRentInfo = null;
        List<RentBreakUp> listRentBreakUp = null;
        CurrentMonthEbInfo currentMonthEbInfo = null;
        List<CurrentMonthOtherItems> listCurrentMonthOtherItems = null;
        if (settlementItems.getCurrentRentBreakUps() != null) {
            if (!settlementItems.getCurrentRentBreakUps().isEmpty()) {
                listRentBreakUp = settlementItems
                        .getCurrentRentBreakUps()
                        .stream()
                        .map(i -> new CurrentRentBreakUp().apply(i))
                        .toList();
            }
        }

        if (settlementItems.getCurrentMonthPayableAmount() != null) {
            if (settlementItems.getIsFullRentCollected() != null) {
                if (settlementItems.getIsFullRentCollected()) {
                    currentPayablemount = settlementItems.getFullRent();
                }
                else {
                    currentPayablemount = settlementItems.getCurrentMonthPayableAmount();
                }
            }
            else {
                currentPayablemount = settlementItems.getCurrentMonthPayableAmount();
            }
        }
        if (settlementItems.getCurrentMonthPaidAmount() != null) {
            currentPaidAmount = settlementItems.getCurrentMonthPaidAmount();
            currentPayablemount = currentPayablemount - currentPaidAmount;
//            subTotal = subTotal - currentPaidAmount;
        }
        if (listRentBreakUp != null) {
            if (!listRentBreakUp.isEmpty()) {
                stayDays = listRentBreakUp
                        .stream()
                        .mapToInt(i -> (int) i.noOfDays())
                        .sum();
            }
        }

        if (settlementItems.getCurrentMonthOtherItems() != null) {
            currentMonthOtherAmounts = settlementItems
                    .getCurrentMonthOtherItems()
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getAmount() != null) {
                            return i.getAmount();
                        }
                        return 0.0;
                    })
                    .sum();
        }

        if (settlementItems.getCurrentMonthOtherItems() != null) {
            if (!settlementItems.getCurrentMonthOtherItems().isEmpty()) {
                listCurrentMonthOtherItems = settlementItems
                        .getCurrentMonthOtherItems()
                        .stream()
                        .map(i -> new CurrentMonthOtherItems(i.getOtherItem(), i.getAmount()))
                        .toList();
            }
        }

        if (settlementItems.getEbItems() != null) {
            if (!settlementItems.getEbItems().isEmpty()) {
                List<EBItems> ebItemsList = settlementItems
                        .getEbItems()
                        .stream()
                        .map(i -> {
                            return new EBItems(i.getReadingId(), i.getCustomerEBId(), i.getFromDate(), i.getToDate(), i.getTotalAmount(), i.getConsumption());
                        })
                        .toList();
                double ebTotalAmount = settlementItems
                        .getEbItems()
                        .stream()
                        .mapToDouble(i -> {
                            if (i.getTotalAmount() == null) {
                                return 0.0;
                            }
                            return i.getTotalAmount();
                        })
                        .sum();
                electricityAmount = ebTotalAmount;

                currentMonthEbInfo = new CurrentMonthEbInfo(ebTotalAmount, ebItemsList);
            }
        }

        if (settlementItems.getWalltetItems() != null) {
            int noOfWalletItems = settlementItems.getWalltetItems().size();
            List<WalltetItems> listWalletItems = settlementItems.getWalltetItems();
            double walletAmount = listWalletItems
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getAmount() != null) {
                            return i.getAmount();
                        }
                        return 0.0;
                    })
                    .sum();
            List<WalletItems> listwalletItems = listWalletItems
                    .stream()
                    .map(i -> new WalletItems(i.getType(), i.getType(), Utils.roundOffWithTwoDigit(i.getAmount())))
                    .toList();
            walletInfo = new WalletInfo(noOfWalletItems, Utils.roundOffWithTwoDigit(walletAmount), listwalletItems);
        }

        currentRentInfo = new CurrentRentInfo(currentPaidAmount,
                currentPayablemount,
                stayDays,
                currentMonthPayableRent,
                currentMonthOtherAmounts,
                listRentBreakUp,
                listCurrentMonthOtherItems);

        FinalSettlementHeaderInfo headerInfo = null;
        if (hostelV1 != null) {
            headerInfo = new FinalSettlementHeaderInfo(invoicesV1.getInvoiceNumber(),
                    hostelV1.getHouseNo(),
                    hostelV1.getStreet(),
                    hostelV1.getCity(),
                    hostelV1.getState(),
                    hostelV1.getPincode(),
                    null,
                    hostelV1.getMobile(),
                    "91",
                    hostelV1.getMainImage(),
                    hostelV1.getEmailId());
        }

        subTotal = subTotal + Utils.roundOfDouble(invoicesV1.getTotalAmount()) + deductionAmount;
        finalAmount = Utils.roundOfDouble(invoicesV1.getTotalAmount()) + deductionAmount;
        finalAmount = finalAmount + unpaidInvoiceAmount + electricityAmount ;


        FinalSettlementInvoiceInfo invoiceInfo = null;
        if (invoicesV1 != null) {
            invoiceInfo = new FinalSettlementInvoiceInfo(invoicesV1.getInvoiceNumber(),
                    Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                    Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                    null,
                    null,
                    Utils.roundOffWithTwoDigit(subTotal),
                    deductionAmount,
                    unpaidInvoiceAmount,
                    electricityAmount,
                    Utils.roundOfDouble(invoicesV1.getTotalAmount()),
                    true,
                    invoicesV1.getPaymentStatus());
        }

        StayInfo stayInfo = null;
        CustomersBedHistory bedHistory = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());

        if (bedHistory != null) {
            BedDetails bedDetails = bedsService.getBedFullDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                stayInfo = new StayInfo(bedDetails.getBedName(),
                        bedDetails.getFloorName(),
                        bedDetails.getRoomName(), "");
            }
        }

        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());


        FinalSettlementInfo finalSettlementResponse = new FinalSettlementInfo(headerInfo,
                stayInfo,
                null,
                templateInfo,
                customerInfo,
                unpaidInvoiceInfo,
                deductionsInfo,
                advanceItems,
                bookingItems,
                currentRentInfo,
                currentMonthEbInfo,
                walletInfo,
                invoiceInfo);

        return finalSettlementResponse;
    }

    private double calculateBalance(double totalAmount, double paidAmount,
                                    String paymentStatus) {
        return switch (paymentStatus) {
            case "PARTIAL_REFUND", "REFUNDED" -> totalAmount + paidAmount;

            default -> totalAmount - paidAmount;
        };
    }

    public InvoicesV1 getInvoice(String invoiceId) {
        return invoicesV1Repository.getReferenceById(invoiceId);
    }

    public List<InvoicesV1> getInvoicesByIds(List<String> listInvoicesId) {
        return invoicesV1Repository.findAllById(listInvoicesId);
    }

    public ResponseEntity<?> getInvoiceReportDetailDetails(String hostelId, String startDate, String endDate) {
        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, startDate, endDate);
        return new ResponseEntity<>(invoicePdfResponse, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReport(String hostelId, String startDate, String endDate) {
        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, startDate, endDate);
        Context context = new Context();
        context.setVariable("invoices", invoicePdfResponse);

        String invoiceReportUrl = pdfServices.generateInvoicePdf("invoice-report", context);
        return new ResponseEntity<>(invoiceReportUrl, HttpStatus.OK);
    }

    public InvoicePdfResponse getInvoiceDetails(String hostelId, String startDate, String endDate) {
        Date sDate = Utils.stringToDate(startDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        List<InvoicesV1> listInvoices = invoicesV1Repository.findByHostelId(hostelId, sDate, eDate);
        List<String> customerIds = listInvoices
                .stream()
                .map(InvoicesV1::getCustomerId)
                .toList();

        List<Customers> customers = customerServices.findByCustomerIds(customerIds);

        List<ListInvoiceItems> listInvoiceItems = listInvoices
                .stream()
                .map(i -> new InvoiceMapper(customers).apply(i))
                .toList();

        String totalInvoice = String.valueOf(listInvoices.size());
        double totalInvoiceAmount = listInvoices.
                stream()
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) {
                        return 0.0;
                    }
                    else {
                        if (i.getTotalAmount() < 0) {
                            return i.getTotalAmount() * -1;
                        }
                        else {
                            return i.getTotalAmount();
                        }
                    }
                })
                .sum();
        double paidAmount = listInvoices
                .stream()
                .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) ||
                        i.getPaymentStatus().equalsIgnoreCase(PARTIAL_PAYMENT.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    else {
                        return 0.0;
                    }
                })
                .sum();

        double returnInvoiceAmount = listInvoices
                .stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    return i.getTotalAmount();
                })
                .sum();

        double cancelledAmount = listInvoices
                .stream()
                .filter(InvoicesV1::isCancelled)
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();

        double outstandingAmount = listInvoices
                .stream()
                .filter(i -> !i.isCancelled())
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();

        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));
        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);
        InvoiceHeader invoiceHeader = new InvoiceHeader(Utils.dateToString(sDate), Utils.dateToString(eDate),
                totalInvoice,
                String.valueOf(totalInvoiceAmount),
                String.valueOf(paidAmount),
                String.valueOf(outstandingAmount),
                String.valueOf(returnInvoiceAmount),
                String.valueOf(cancelledAmount));

        return new InvoicePdfResponse(hostelInformation,
                invoiceHeader,
                footerInfo,
                listInvoiceItems);
    }

    public ResponseEntity<?> getNewSettlementDetails(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FinalSettlementInfo finalSettlementInfo = newSettlementInvoice(invoicesV1);
        return new ResponseEntity<>(finalSettlementInfo, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReportNew(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostelV1 = hostelService.getHostel(invoicesV1.getHostelId());
        FinalSettlementInfo finalSettlementInfo = newSettlementInvoice(invoicesV1);
        Context context = new Context();
        context.setVariable("invoice", finalSettlementInfo);

        String invoiceUrl = invoicePDFServices.generatePdf(invoiceId, "invoice-settlement", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    public AdvanceItems getRedeemedListFromAdvance(String hostelId, String customerId) {
        InvoicesV1 advanceInvoice = invoicesV1Repository.findAdvanceInvoiceByCustomerId(customerId);
        if (advanceInvoice == null) {
            return new AdvanceItems("Refundable Advance",
                    0.0,
                    0.0,
                    0.0,
                    "NA",
                    null);
        }
        if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
            return new AdvanceItems("Refundable Advance",
                    0.0,
                    0.0,
                    0.0,
                    advanceInvoice.getInvoiceNumber(),
                    null);
        }

        Double paidAmount = 0.0;
        Double availableAmount = 0.0;
        if (advanceInvoice.getPaidAmount() != null) {
            paidAmount = advanceInvoice.getPaidAmount();
        }
        if (availableAmount != null) {
            availableAmount = advanceInvoice.getBalanceAmount();
        }

        List<RedeemedInfo> listRedeemedInfo = invoiceRedemptionService.findRedeemedItemsFromAdvance(hostelId, advanceInvoice.getInvoiceId());

        return new AdvanceItems("Refundable Advance",
                availableAmount,
                paidAmount - availableAmount,
                paidAmount,
                advanceInvoice.getInvoiceNumber(),
                listRedeemedInfo);
    }

    public AdvanceItems getRedeemedListFromBookings(String hostelId, String customerId) {
        InvoicesV1 advanceInvoice = invoicesV1Repository.findBookingInvoice(hostelId, customerId);
        if (advanceInvoice == null) {
            return new AdvanceItems("Refundable Bookings",
                    0.0,
                    0.0,
                    0.0,
                    "NA",
                    null);
        }
        if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
            return new AdvanceItems("Refundable Bookings",
                    0.0,
                    0.0,
                    0.0,
                    advanceInvoice.getInvoiceNumber(),
                    null);
        }

        Double paidAmount = 0.0;
        Double availableAmount = 0.0;
        if (advanceInvoice.getPaidAmount() != null) {
            paidAmount = advanceInvoice.getPaidAmount();
        }
        if (availableAmount != null) {
            availableAmount = advanceInvoice.getBalanceAmount();
        }

        List<RedeemedInfo> listRedeemedInfo = invoiceRedemptionService.findRedeemedItemsFromAdvance(hostelId, advanceInvoice.getInvoiceId());

        return new AdvanceItems("Refundable Bookings",
                availableAmount,
                paidAmount - availableAmount,
                paidAmount,
                advanceInvoice.getInvoiceNumber(),
                listRedeemedInfo);
    }

    public List<InvoicesV1> findInvoices(List<String> targetInvoiceIds) {
        return invoicesV1Repository.findByInvoiceIdIn(targetInvoiceIds);
    }
}
