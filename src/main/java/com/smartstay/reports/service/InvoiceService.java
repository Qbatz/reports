package com.smartstay.reports.service;

import com.smartstay.reports.dao.*;
import com.smartstay.reports.dto.beds.BedDetails;
import com.smartstay.reports.dto.billingRules.BillingDates;
import com.smartstay.reports.dto.customer.Deductions;
import com.smartstay.reports.dto.customer.FooterInfo;
import com.smartstay.reports.dto.customer.HostelInformation;
import com.smartstay.reports.dto.customer.StayInfo;
import com.smartstay.reports.dto.invoice.*;
import com.smartstay.reports.dto.invoice.EBItems;
import com.smartstay.reports.dto.settlement.*;
import com.smartstay.reports.ennum.*;
import com.smartstay.reports.payloads.invoice.InvConsolidatedViewPayload;
import com.smartstay.reports.repositories.InvoicesV1Repository;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.filterOptions.ColumnFiltersRes;
import com.smartstay.reports.responses.hostel.HostelInfo;
import com.smartstay.reports.responses.hostel.InvoiceHeader;
import com.smartstay.reports.responses.hostel.ListInvoiceItems;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import com.smartstay.reports.responses.invoice.*;
import com.smartstay.reports.responses.invoice.InvoiceItems;
import com.smartstay.reports.responses.invoice.RetainerInfo;
import com.smartstay.reports.responses.pagination.PaginationDataRes;
import com.smartstay.reports.responses.settlement.AdditionalAdvance;
import com.smartstay.reports.responses.settlement.AdditionalAdvanceItems;
import com.smartstay.reports.services.InvoiceRedemptionService;
import com.smartstay.reports.services.SettlementItemService;
import com.smartstay.reports.utils.NameUtils;
import com.smartstay.reports.utils.Utils;
import com.smartstay.reports.utils.columnOptions.InvRegisterConsolidatedColumnUtils;
import com.smartstay.reports.wrappers.CurrentRentBreakUp;
import com.smartstay.reports.wrappers.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private BankingV2Service bankingV2Service;
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private BillingRulesService billingRulesService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private TableColumnService tableColumnService;

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
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name()) ||
                invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
            return getRetainerInvoice(invoicesV1);
        }

        InvoiceInfo invoiceInfo = getInvoiceInfo(invoicesV1);
        Context context = new Context();
        context.setVariable("invoice", invoiceInfo);

        String invoiceUrl = invoicePDFServices.generatePdf(invoiceId, "invoice", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    private ResponseEntity<?> getRetainerInvoice(InvoicesV1 invoicesV1) {

        RetainerInfo retainerInfo = getRetainerInfo(invoicesV1);

        Context context = new Context();
        context.setVariable("invoice", retainerInfo);

        String invoiceUrl = invoicePDFServices
                .generatePdf(invoicesV1.getInvoiceId(), "retainer-invoice", context);

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceDetails(String hostelId, String invoiceId) {

        InvoicesV1 invoicesV1 = invoicesV1Repository
                .findByHostelIdAndInvoiceId(hostelId, invoiceId);
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
        String rentalPeriod = Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate()) + "-" +
                Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate());

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

            invoiceItems.add(new InvoiceItems(InvoiceType.SETTLEMENT.name(), String.valueOf(invoicesV1.getBasePrice()),
                    invoicesV1.getInvoiceNumber()));
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
                            item = "Electricity Bill";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.AMENITY.name())) {
                            item = "Amenity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.BOOKING.name())) {
                            item = "Booking";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.ADVANCE.name())) {
                            item = "Advance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.MAINTENANCE.name())) {
                            item = "Maintenance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                            if (i.getOtherItem().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
                                item = "Advance EB";
                            }
                            else if (i.getOtherItem().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
                                item = "Advance Rent";
                            }
                            else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(i.getOtherItem())) {
                                item = "Additional Advance";
                            } else {
                                item = i.getOtherItem();
                            }
                        }

                        amount = String.valueOf(Math.round(i.getAmount()));

                        return new InvoiceItems(item, amount, invoicesV1.getInvoiceNumber());
                    })
                    .toList();

            listDeductions = invoicesV1.getDeductions();
            if (invoicesV1.getDeductionAmount() != null) {
                totalDeductionAmount = invoicesV1.getDeductionAmount() != null ? invoicesV1.getDeductionAmount() : 0;
            }


        }

        String invoiceType = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign Rent Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Security Deposit(Booking) Invoice";
            rentalPeriod = "";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Security Deposit(Advance) Invoice";
            rentalPeriod = "";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Final Settlement Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name()) || 
                invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
            invoiceType = "Retainer Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.OTHER.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Other Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Additional Advance Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.EB.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Eb Invoice";
            rentalPeriod = "";
        }
        else {
            invoiceType = Utils.capitalize(invoicesV1.getInvoiceType()) + " Invoice";
        }

        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        CustomersBedHistory cbh = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            cbh = customerBedHistoryService.getBookedBed(invoicesV1.getCustomerId());
        }
        else {
            cbh = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(),
                    invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        }
        BedInfo bedInfo = null;
        if (cbh != null) {
            bedInfo = bedsService.getBedDetails(cbh.getBedId());
        }

        AccountDetails accountDetails = new AccountDetails("", "", "", "", "");
        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());
        if (templateInfo != null) {
            if (templateInfo.bankId() != null) {
                accountDetails = bankingV2Service.getBankAccountDetails(templateInfo.bankId(), templateInfo.qrCode());
            }
        }

        InvoiceInfo invoiceInfo = new InvoiceInfo(
                invoicesV1.getInvoiceNumber(),
                invoiceDate,
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                rentalPeriod,
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(totalDeductionAmount)),
                String.valueOf(Math.round(paidAmount)),
                String.valueOf(Math.round(balanceAmount)),
                String.valueOf(Math.round(invoicesV1.getBasePrice())),
                String.valueOf(Math.round(invoicesV1.getTotalAmount())),
                String.valueOf(Math.round(discount)),
                invoiceType,
                Utils.capitalize(invoicesV1.getPaymentStatus()),
                invoiceItems,
                listDeductions,
                hostelInfo,
                customerInfo,
                bedInfo,
                templateInfo,
                accountDetails
        );

        return invoiceInfo;
    }

    private FinalSettlementInfo newSettlementInvoice(InvoicesV1 invoicesV1) {

        HostelV1 hostelV1 = hostelService.getHostel(invoicesV1.getHostelId());

        SettlementItems settlementItems = settlementItemService
                .getSettlementItems(invoicesV1.getInvoiceId());

        double subTotal = 0.0;
        double deductionAmount = 0.0;
        double discountAmount = 0.0;
        double unpaidInvoiceAmount = 0.0;
        double electricityAmount = 0.0;
        double finalAmount = 0.0;
        double refundable = 0.0;
        double nonRefundable = 0.0;
        double payableRent = 0.0;

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
                            double totalAmount = i.getInvoiceAmount() != null ? i.getInvoiceAmount() : 0;
                            double pendingAmount = i.getPendingAmount() != null ? i.getPendingAmount() : 0;
                            double paidAmount = totalAmount - pendingAmount;
                            return new UnpaidInvoiceItem(i.getInvoiceNo(), totalAmount, paidAmount, pendingAmount);
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
                            double pendAmount = 0.0;
                            double amount = i.getAmount() != null ? i.getAmount() : 0;
                            double payAmount = i.getPaidAmount() != null ? i.getPaidAmount() : 0;

                            pendAmount = amount - payAmount;

                            return new DeductionsItem(Utils.capitalize(i.getType()), payAmount, amount, pendAmount);
                        })
                        .toList();

                deductionsInfo = new DeductionsInfo(totalDeductionAmount, paidAmount,
                        pendingAmount, listDeductionItems);
            }
        }

        AdvanceItems advanceItems = getRedeemedListFromAdvance(invoicesV1.getHostelId(),
                invoicesV1.getCustomerId());
        AdvanceItems bookingItems = getRedeemedListFromBookings(invoicesV1.getHostelId(),
                invoicesV1.getCustomerId());

        if (advanceItems != null) {
            if (advanceItems.availableAdvanceBalance() != null) {
                refundable = advanceItems.availableAdvanceBalance();
//                subTotal = subTotal - advanceItems.availableAdvanceBalance();
            }
        }
        if (bookingItems != null) {
            if (bookingItems.availableAdvanceBalance() != null) {
//                subTotal = subTotal - bookingItems.availableAdvanceBalance();
                refundable = refundable + bookingItems.availableAdvanceBalance();
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
                    currentPayablemount = settlementItems.getFullRent() != null
                            ? settlementItems.getFullRent() : 0;
                }
                else {
                    currentPayablemount = settlementItems.getCurrentMonthPayableAmount();
                }
            }
            else {
                currentPayablemount = settlementItems.getCurrentMonthPayableAmount();
            }
        }

        nonRefundable = nonRefundable + currentPayablemount;

        if (settlementItems.getCurrentMonthPaidAmount() != null) {
            currentPaidAmount = settlementItems.getCurrentMonthPaidAmount();
            currentPayablemount = currentPayablemount - currentPaidAmount;
//            subTotal = subTotal - currentPaidAmount;
        }

//        if (currentPayablemount > 0) {
//            payableRent = Utils.roundOffWithTwoDigit(currentPayablemount);
//        }
//        else {
//            refundable = refundable + (currentPayablemount * -1);
//        }

        refundable = refundable + currentPaidAmount;

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
                            return new EBItems(i.getReadingId(), i.getCustomerEBId(), i.getFromDate(), i.getToDate(),
                                    i.getTotalAmount(), i.getConsumption());
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

            if (walletAmount <= 0) {
                refundable = refundable + walletAmount;
            }

            List<WalletItems> listwalletItems = listWalletItems
                    .stream()
                    .map(i -> new WalletItems(i.getType(), i.getType(),
                            Utils.roundOffWithTwoDigit(i.getAmount() != null ? i.getAmount() : 0)))
                    .toList();

            walletInfo = new WalletInfo(noOfWalletItems, Utils.roundOffWithTwoDigit(walletAmount),
                    listwalletItems);
        }

        currentRentInfo = new CurrentRentInfo(currentPaidAmount,
                Utils.roundOffWithTwoDigit(currentPayablemount),
                stayDays,
                currentMonthPayableRent,
                currentMonthOtherAmounts,
                listRentBreakUp,
                listCurrentMonthOtherItems);

        com.smartstay.reports.dto.settlement.RetainerInfo retainerInfo = null;
        if (settlementItems.getRetainerItems() != null) {

            List<RetainerItems> listRetainerItems = settlementItems.getRetainerItems();

            double totalRetainerApplied = listRetainerItems
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getAmount() != null) {
                            return i.getAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            refundable = refundable + totalRetainerApplied;

            double totalRetainerAmount = listRetainerItems
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getTotalAmount() != null) {
                            return i.getTotalAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            List<RetainerItemsList> items = listRetainerItems
                    .stream()
                    .map(i -> new RetainerItemsList(i.getInvoiceNo(),
                            i.getInvoiceDate(),
                            String.valueOf(Utils.roundOffWithTwoDigit(
                                    i.getAmount() != null ?  i.getAmount() : 0
                            ))))
                    .toList();

            retainerInfo = new com.smartstay.reports.dto.settlement.RetainerInfo(
                    String.valueOf(Utils.roundOffWithTwoDigit(totalRetainerApplied)),
                    String.valueOf(Utils.roundOffWithTwoDigit(totalRetainerAmount)),
                    items);
        }

        AdditionalAdvance additionalAdvance = null;
        if (settlementItems.getAdditionalAdvanceItems() != null) {

            List<com.smartstay.reports.dto.settlement.AdditionalAdvance> listAdditionalAdvance =
                    settlementItems.getAdditionalAdvanceItems();

            double totalPaidAmount = listAdditionalAdvance
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() != null) {
                            return i.getPaidAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            double additionalRefundable = listAdditionalAdvance
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getInvoiceBalance() != null) {
                            return i.getInvoiceBalance();
                        }
                        return 0.0;
                    })
                    .sum();

            refundable = refundable + additionalRefundable;

            List<AdditionalAdvanceItems> listAdditionalAdvanceItesm = listAdditionalAdvance
                    .stream()
                    .map(i -> new AdditionalAdvanceItems(i.getInvoiceId(),
                            i.getInvoiceNumber(), i.getInvoiceBalance()))
                    .toList();

            additionalAdvance = new AdditionalAdvance(listAdditionalAdvance.size(),
                    Utils.roundOffWithTwoDigit(totalPaidAmount),
                    Utils.roundOffWithTwoDigit(additionalRefundable),
                    listAdditionalAdvanceItesm);
        }

        FinalSettlementHeaderInfo headerInfo = null;
        if (hostelV1 != null) {
            headerInfo = new FinalSettlementHeaderInfo(invoicesV1.getInvoiceNumber(),
                    hostelV1.getHostelName(),
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

        nonRefundable = nonRefundable + unpaidInvoiceAmount + electricityAmount;

        discountAmount = invoiceDiscountService
                .getInoiceDiscount(hostelV1 != null ? hostelV1.getHostelId() : "", invoicesV1.getInvoiceId());

        subTotal = subTotal + Utils.roundOfDouble(invoicesV1.getTotalAmount() != null
                ? invoicesV1.getTotalAmount() : 0) + deductionAmount;
        finalAmount = Utils.roundOfDouble(invoicesV1.getTotalAmount() != null
                ? invoicesV1.getTotalAmount() : 0) + deductionAmount;
        finalAmount = finalAmount + unpaidInvoiceAmount + electricityAmount ;

        FinalSettlementInvoiceInfo invoiceInfo = null;
        if (invoicesV1 != null) {
            invoiceInfo = new FinalSettlementInvoiceInfo(invoicesV1.getInvoiceNumber(),
                    Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                    Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                    null,
                    null,
                    Utils.roundOffWithTwoDigit(payableRent),
                    Utils.roundOffWithTwoDigit(subTotal),
                    deductionAmount,
                    unpaidInvoiceAmount,
                    electricityAmount,
                    discountAmount,
                    Utils.roundOfDouble(invoicesV1.getTotalAmount() != null
                            ? invoicesV1.getTotalAmount() : 0),
                    Utils.roundOffWithTwoDigit(refundable),
                    Utils.roundOffWithTwoDigit(nonRefundable),
                    true,
                    invoicesV1.getPaymentStatus());
        }

        StayInfo stayInfo = null;
        CustomersBedHistory bedHistory = customerBedHistoryService
                .getCustomerBedByStartDate(invoicesV1.getCustomerId(), invoicesV1.getInvoiceStartDate(),
                        invoicesV1.getInvoiceEndDate());

        if (bedHistory != null) {
            BedDetails bedDetails = bedsService.getBedFullDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                stayInfo = new StayInfo(bedDetails.getBedName(),
                        bedDetails.getFloorName(),
                        bedDetails.getRoomName(), "");
            }
        }

        TemplateInfo templateInfo = templateService
                .getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());

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
                additionalAdvance,
                currentRentInfo,
                currentMonthEbInfo,
                retainerInfo,
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
        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, null, null, null, null, null, null, null, null, null, null, startDate, endDate);
        return new ResponseEntity<>(invoicePdfResponse, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReport(String hostelId,
                                              String search,
                                              List<String> paymentStatus,
                                              List<String> invoiceModes,
                                              List<String> invoiceTypes,
                                              List<String> createdBy,
                                              String period,
                                              Double minPaidAmount,
                                              Double maxPaidAmount,
                                              Double minOutstandingAmount,
                                              Double maxOutstandingAmount,
                                              String startDate,
                                              String endDate) {

        InvoicePdfResponse invoicePdfResponse = getInvoiceDetails(hostelId, search, paymentStatus,
                invoiceModes, invoiceTypes, createdBy, period, minPaidAmount, maxPaidAmount, minOutstandingAmount,
                maxOutstandingAmount, startDate, endDate);

        Context context = new Context();
        context.setVariable("invoices", invoicePdfResponse);

        String invoiceReportUrl = pdfServices
                .generateInvoicePdf("invoice-report", context);

        return new ResponseEntity<>(invoiceReportUrl, HttpStatus.OK);
    }

    public InvoicePdfResponse getInvoiceDetails(String hostelId,
                                                String search,
                                                List<String> paymentStatus,
                                                List<String> invoiceModes,
                                                List<String> invoiceTypes,
                                                List<String> createdBy,
                                                String period,
                                                Double minPaidAmount,
                                                Double maxPaidAmount,
                                                Double minOutstandingAmount,
                                                Double maxOutstandingAmount,
                                                String startDate,
                                                String endDate) {

        Date sDate = Utils.stringToDate(startDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date eDate = Utils.stringToDate(endDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        if (invoiceTypes == null || invoiceTypes.isEmpty()) {
            invoiceTypes = new ArrayList<>();
            invoiceTypes.add(InvoiceType.RENT.name());
            invoiceTypes.add(InvoiceType.ADVANCE.name());
            invoiceTypes.add(InvoiceType.REASSIGN_RENT.name());
            invoiceTypes.add(InvoiceType.BOOKING.name());
            invoiceTypes.add(InvoiceType.OTHER.name());
            invoiceTypes.add(InvoiceType.EB.name());
            invoiceTypes.add(InvoiceType.ADDITIONAL_ADVANCE.name());
        }

        if (paymentStatus != null) {
            if (paymentStatus.contains("ALL")) {
                paymentStatus = null;
            }
        }

        List<InvoicesV1> listInvoices;
        if (search != null && !search.isEmpty()) {
            List<com.smartstay.reports.dao.Customers> customersList = customerServices.searchCustomer(hostelId, search);
            List<String> searchCustomerIds = customersList.stream().map(com.smartstay.reports.dao.Customers::getCustomerId).toList();
            if (searchCustomerIds.isEmpty()) {
                listInvoices = new ArrayList<>();
            } else {
                listInvoices = invoicesV1Repository.findInvoicesByFilters(hostelId, sDate, eDate, searchCustomerIds,
                        paymentStatus, invoiceModes, invoiceTypes, createdBy,
                        minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);
            }
        } else {
            listInvoices = invoicesV1Repository.findInvoicesByFilters(hostelId, sDate, eDate, null,
                    paymentStatus, invoiceModes, invoiceTypes, createdBy,
                    minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);
        }
        
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
                        i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
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
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) &&
                        i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
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

        double totalBookingAmount = listInvoices.stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) return 0.0;
                    if (i.getTotalAmount() < 0) return i.getTotalAmount() * -1;
                    return i.getTotalAmount();
                })
                .sum();

        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(new Date()), Utils.dateToTime(new Date()));

        HostelInformation hostelInformation = hostelService.getHostelInformation(hostelId);

        InvoiceHeader invoiceHeader = new InvoiceHeader(Utils.dateToString(sDate), Utils.dateToString(eDate),
                totalInvoice, String.valueOf(totalInvoiceAmount), String.valueOf(paidAmount),
                String.valueOf(Utils.roundOffWithTwoDigit(totalBookingAmount)), String.valueOf(outstandingAmount),
                String.valueOf(returnInvoiceAmount), String.valueOf(cancelledAmount));

        return new InvoicePdfResponse(hostelInformation, invoiceHeader, footerInfo, listInvoiceItems);
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

        HostelV1 hostel = hostelService.getHostel(invoicesV1.getHostelId());

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
        if (advanceInvoice.getBalanceAmount() != null) {
            availableAmount = advanceInvoice.getBalanceAmount();
        }

        List<RedeemedInfo> listRedeemedInfo = invoiceRedemptionService
                .findRedeemedItemsFromAdvance(hostelId, advanceInvoice.getInvoiceId());

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
        if (advanceInvoice.getBalanceAmount() != null) {
            availableAmount = advanceInvoice.getBalanceAmount();
        }

        List<RedeemedInfo> listRedeemedInfo = invoiceRedemptionService
                .findRedeemedItemsFromAdvance(hostelId, advanceInvoice.getInvoiceId());

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

    public ResponseEntity<?> getNewRetainerDetails(String hostelId, String invoiceId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        RetainerInfo invoiceInfo = getRetainerInfo(invoicesV1);

        return new ResponseEntity<>(invoiceInfo, HttpStatus.OK);
    }

    private RetainerInfo getRetainerInfo(InvoicesV1 invoicesV1) {

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
        String rentalPeriod = Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate()) + "-"
                + Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate());

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
                            item = "Electricity Bill";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.AMENITY.name())) {
                            item = "Amenity";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.BOOKING.name())) {
                            item = "Booking";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.ADVANCE.name())) {
                            item = "Advance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.MAINTENANCE.name())) {
                            item = "Maintenance";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
                            item = "Advance EB";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
                            item = "Advance Rent";
                        }
                        else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.reports.ennum.InvoiceItems.OTHERS.name())) {
                            if (i.getOtherItem().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
                                item = "Advance EB";
                            }
                            else if (i.getOtherItem().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
                                item = "Advance Rent";
                            }
                            else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(i.getOtherItem())) {
                                item = "Additional Advance";
                            } else {
                                item = i.getOtherItem();
                            }
                        }

                        amount = String.valueOf(Math.round(i.getAmount()));

                        return new InvoiceItems(item, amount, invoicesV1.getInvoiceNumber());
                    })
                    .toList();
        }

        String invoiceType = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign Rent Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Security Deposit(Booking) Invoice";
            rentalPeriod = "";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Security Deposit(Advance) Invoice";
            rentalPeriod = "";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Final Settlement Invoice";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name()) ||
                invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
            invoiceType = "Retainer Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.OTHER.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Other Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Additional Advance Invoice";
            rentalPeriod = "";
        }
        else if (InvoiceType.EB.name().equalsIgnoreCase(invoicesV1.getInvoiceType())) {
            invoiceType = "Eb Invoice";
            rentalPeriod = "";
        }
        else {
            invoiceType = Utils.capitalize(invoicesV1.getInvoiceType()) + " Invoice";
        }

        HostelInfo hostelInfo = hostelService.hostelInfo(invoicesV1.getHostelId());
        CustomerInfo customerInfo = customerServices.getCustomerInfo(invoicesV1.getCustomerId());
        CustomersBedHistory cbh = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            cbh = customerBedHistoryService.getBookedBed(invoicesV1.getCustomerId());
        }
        else {
            cbh = customerBedHistoryService.getCustomerBedByStartDate(invoicesV1.getCustomerId(),
                    invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        }
        BedInfo bedInfo = null;
        if (cbh != null) {
            bedInfo = bedsService.getBedDetails(cbh.getBedId());
        }

        AccountDetails accountDetails = new AccountDetails("", "", "", "", "");
        TemplateInfo templateInfo = templateService.getTemplateDetails(invoicesV1.getHostelId(), invoicesV1.getInvoiceType());
        if (templateInfo != null) {
            if (templateInfo.bankId() != null) {
                accountDetails = bankingV2Service.getBankAccountDetails(templateInfo.bankId(), templateInfo.qrCode());
            }
        }

        RetainerInfo invoiceInfo = new RetainerInfo(
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
                templateInfo,
                accountDetails
        );

        return invoiceInfo;
    }

    public List<String> findInvoiceIdsByHostelIdAndTypeIn(String hostelId, List<String> invoiceTypes) {
        List<String> listInvoiceIds = invoicesV1Repository.findInvoiceIdsByHostelIdAndTypeIn(hostelId, invoiceTypes);
        if (listInvoiceIds == null) {
            return new ArrayList<>();
        }
        return listInvoiceIds;
    }


    public ResponseEntity<?> getInvoiceReportConsolidatedView(String hostelId, String userId,
                                                              InvConsolidatedViewPayload payload) {

        return buildInvoiceReportConsolidatedView(hostelId, userId, payload);
    }

    private ResponseEntity<?> buildInvoiceReportConsolidatedView(String hostelId, String userId,
                                                                 InvConsolidatedViewPayload payload) {

        HostelV1 hostel = hostelService.getHostel(hostelId);
        if (hostel == null){
            return new ResponseEntity<>("Hostel not found", HttpStatus.BAD_REQUEST);
        }

        Users user = usersService.findByUserId(userId);
        if (user == null){
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }

        BillingRules billingRules = billingRulesService
                .getLatestBillingRulesByHostelId(hostelId);
        if (billingRules == null){
            return new ResponseEntity<>("Billing rules not found", HttpStatus.BAD_REQUEST);
        }

        List<ColumnFilters> tableColumns = tableColumnService
                .getInvRegConsolidatedColumns(hostelId, userId,
                        FilterOptionsModule.MODULE_INVOICE_REGISTER_CONSOLIDATED.name());

        List<ColumnFilters> activeColumns = tableColumns.stream()
                .filter(ColumnFilters::isSelected)
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .toList();

        List<String> columnNames = activeColumns.stream()
                .map(ColumnFilters::getFieldName)
                .toList();

        List<ColumnFiltersRes> activeColumnsRes = activeColumns.stream()
                .map(i -> {

                    boolean isLocked = InvRegisterConsolidatedColumnUtils.FLOOR.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.ROOM.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.BED.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.TOTAL_PENDING_AMOUNT.equals(i.getFieldName());

                    return new ColumnFiltersRes(i.getOrder(), i.getFieldName(),
                            i.isSelected(), isLocked);
                }).toList();

        Date today = new Date();

        Boolean isPaginated = payload.isPaginated();
        int page = payload.page();
        int size = payload.size();
        String search = payload.search();
        List<String> paymentStatus = payload.paymentStatus();
        List<String> invoiceModes = payload.invoiceModes();
        List<String> invoiceTypes = payload.invoiceTypes();
        List<String> createdBy = payload.createdBy();
        String period = payload.period();
        Double minPaidAmount = payload.minPaidAmount();
        Double maxPaidAmount = payload.maxPaidAmount();
        Double minOutstandingAmount = payload.minOutstandingAmount();
        Double maxOutstandingAmount = payload.maxOutstandingAmount();
        String startDate = payload.startDate();
        String endDate = payload.endDate();
        List<Integer> floorIds = payload.floorIds();
        Boolean includePaidInvoice = payload.includePaidInvoice();
        Boolean includeEmptyBeds = payload.includeEmptyBeds();
        String groupBy = payload.groupBy();
        Boolean includeSubTotals = payload.includeSubTotals();
        Boolean includeGrandTotal = payload.includeGrandTotal();
        Boolean includeHostelName = payload.includeHostelName();

        if (isPaginated == null){
            isPaginated = false;
        }

        if (isPaginated) {
            page = Math.max(page - 1, 0);
            size = Math.max(size, 1);
        }

        search = (search == null || search.isBlank()) ? null : search.trim();

        Set<String> filteredCustomerIds = null;
        if (search != null){
            List<Customers> customers = customerServices
                    .searchCustomer(hostelId, search);

            filteredCustomerIds = customers.stream()
                    .map(Customers::getCustomerId)
                    .collect(Collectors.toSet());
        }

        if (paymentStatus == null || paymentStatus.isEmpty()){
            paymentStatus = null;
        }

        if (invoiceModes == null || invoiceModes.isEmpty()){
            invoiceModes = null;
        }

        if (invoiceTypes == null || invoiceTypes.isEmpty()){
            invoiceTypes = null;
        }

        if (createdBy == null || createdBy.isEmpty()){
            createdBy = null;
        }

        Date filterStartDate = null;
        Date filterEndDate = null;
        if (period != null && !period.isBlank()){
            BillingDates billingDates = billingRulesService
                    .getBillingDatesByPeriod(period, billingRules);

            filterStartDate = billingDates.currentBillStartDate();
            filterEndDate = billingDates.currentBillEndDate();
        }

        if (startDate != null && endDate != null){
            filterStartDate = Utils.stringToDate(startDate
                    .replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            filterEndDate = Utils.stringToDate(endDate
                    .replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        List<Floors> allFloors = floorsService.getAllByHostelId(hostelId);
        Map<Integer, Floors> allFloorsMap = allFloors.stream()
                .collect(Collectors.toMap(Floors::getFloorId,
                        Function.identity()));

        List<Floors> floors = new ArrayList<>();
        List<Rooms> rooms = new ArrayList<>();
        List<Beds> beds = new ArrayList<>();
        if (floorIds == null || floorIds.isEmpty()){
            floors = allFloors;
        } else {
            floors = floorsService.getByFloorIds(new HashSet<>(floorIds));
        }

        if (floors != null && !floors.isEmpty()){
            Set<Integer> newFloorIds = floors.stream()
                    .map(Floors::getFloorId)
                    .collect(Collectors.toSet());

            rooms = roomsService.getAllByFloorIds(newFloorIds);
        }

        if (rooms != null && !rooms.isEmpty()){
            Set<Integer> roomIds = rooms.stream()
                    .map(Rooms::getRoomId)
                    .collect(Collectors.toSet());

            beds = bedsService.getAllByRoomIds(roomIds);
        }

        if (includeEmptyBeds == null){
            includeEmptyBeds = false;
        }

        List<BookingsV1> latestBookings = new ArrayList<>();
        if (beds != null && !beds.isEmpty()){

            Set<Integer> bedIds = beds.stream()
                    .map(Beds::getBedId)
                    .collect(Collectors.toSet());

            latestBookings = bookingsService
                    .getLatestBookingsByBedIds(bedIds);

            Set<String> bedCustomerIds = latestBookings.stream()
                    .map(BookingsV1::getCustomerId)
                    .collect(Collectors.toSet());

            if (!bedCustomerIds.isEmpty()){
                if (filteredCustomerIds != null){
                    filteredCustomerIds.retainAll(bedCustomerIds);
                } else {
                    filteredCustomerIds = bedCustomerIds;
                }
            }
        }

        if (includePaidInvoice == null){
            includePaidInvoice = false;
        }

        if (includePaidInvoice){
            if (paymentStatus != null){
                paymentStatus.add(PaymentStatus.PAID.name());
            }
        } else {
            if (paymentStatus != null){
                paymentStatus.remove(PaymentStatus.PAID.name());
            }
        }

        GroupByEnum groupByEnum = GroupByEnum.FLOOR_ROOM_BED;
        if (groupBy != null){
            try {
                groupByEnum = GroupByEnum.valueOf(groupBy);
            } catch (Exception e){
                return new ResponseEntity<>("Invalid Group by filter",
                        HttpStatus.BAD_REQUEST);
            }
        }

        if (includeSubTotals == null){
            includeSubTotals = true;
        }

        if (includeGrandTotal == null){
            includeGrandTotal = true;
        }

        if (includeHostelName == null){
            includeHostelName = true;
        }

        List<InvoicesV1> invoices = invoicesV1Repository
                .findConsolidatedInvoicesByFilters(hostelId, filteredCustomerIds, paymentStatus,
                        invoiceModes, invoiceTypes, createdBy, filterStartDate, filterEndDate, minPaidAmount,
                        maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);

        if (paymentStatus == null && !includePaidInvoice){
            invoices = invoices.stream()
                    .filter(i -> !PaymentStatus.PAID.name().equalsIgnoreCase(i.getPaymentStatus()))
                    .toList();
        }

        Map<Integer, List<Rooms>> roomsByFloor = new HashMap<>();
        if (rooms != null){
            roomsByFloor = rooms.stream()
                    .collect(Collectors.groupingBy(Rooms::getFloorId));
        }

        Map<Integer, List<Beds>> bedsByRoom = new HashMap<>();
        if (beds != null){
            bedsByRoom = beds.stream()
                    .collect(Collectors.groupingBy(Beds::getRoomId));
        }

        Map<Integer, String> customerByBed = latestBookings.stream()
                .collect(Collectors.toMap(
                        BookingsV1::getBedId,
                        BookingsV1::getCustomerId,
                        (existing, replacement) -> existing
                ));

        Map<String, List<InvoicesV1>> invoicesByCustomer = invoices.stream()
                .collect(Collectors.groupingBy(InvoicesV1::getCustomerId));

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<Customers> customers = customerServices
                .findByCustomerIds(new ArrayList<>(customerIds));

        Map<String, Customers> customersMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId,
                        customer -> customer));

        Set<String> createdByUserIds = invoices.stream()
                .filter(i -> !InvoiceType.SETTLEMENT.name().equalsIgnoreCase(i.getInvoiceType()))
                .map(InvoicesV1::getCreatedBy)
                .collect(Collectors.toSet());

        List<Users> createdByUsers = usersService
                .findByUserIds(new ArrayList<>(createdByUserIds));

        String totalInvoice = String.valueOf(invoices.size());
        double totalInvoiceAmount = 0;
        double paidAmount = 0;
        double returnInvoiceAmount = 0;
        double cancelledAmount = 0;
        double outstandingAmount = 0;
        double totalBookingAmount = 0;

        for (InvoicesV1 invoice : invoices){

            double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
            double invPaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0;
            double pendingAmount = totalAmount - invPaidAmount;

            if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType())){
                totalBookingAmount += totalAmount;
            }
            if (!invoice.isCancelled()){
                outstandingAmount += pendingAmount;
            } else {
                cancelledAmount += pendingAmount;
            }
            if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType()) &&
                    PaymentStatus.CANCELLED.name().equalsIgnoreCase(invoice.getPaymentStatus())){
                if (invoice.getPaidAmount() != null){
                    returnInvoiceAmount += invoice.getPaidAmount();
                } else {
                    returnInvoiceAmount += totalAmount;
                }
            }
            if (PaymentStatus.PAID.name().equalsIgnoreCase(invoice.getPaymentStatus()) ||
                    PaymentStatus.PARTIAL_PAYMENT.name().equalsIgnoreCase(invoice.getPaymentStatus())){
                paidAmount += invPaidAmount;
            }
            totalInvoiceAmount += totalAmount;
        }

        HostelInformation hostelInfo = hostelService.getHostelInformation(hostelId);

        InvoiceHeader invHeader = new InvoiceHeader(filterStartDate != null ? Utils.dateToString(filterStartDate) : null,
                filterEndDate != null ? Utils.dateToString(filterEndDate) : null, totalInvoice, String.valueOf(totalInvoiceAmount),
                String.valueOf(paidAmount), String.valueOf(Utils.roundOffWithTwoDigit(totalBookingAmount)),
                String.valueOf(outstandingAmount), String.valueOf(returnInvoiceAmount), String.valueOf(cancelledAmount));

        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(today), Utils.dateToTime(today));

        List<UserFilterRes> createdByFilters = createdByUsers.stream()
                .map(i -> new UserFilterRes(NameUtils
                        .getFullName(i.getFirstName(), i.getLastName()),
                        i.getUserId()))
                .toList();

        List<EnumFilterRes> periodFilters = Arrays.stream(PeriodEnum.values())
                .map(i -> new EnumFilterRes(i.name(), i.getLabel()))
                .toList();

        List<EnumFilterRes> paymentStatusFilters = Arrays.stream(PaymentStatus.values())
                .map(i -> new EnumFilterRes(i.name(), i.getDisplayName()))
                .toList();

        List<EnumFilterRes> invoiceModeFilters = Arrays.stream(InvoiceMode.values())
                .map(i -> new EnumFilterRes(i.name(),
                        NameUtils.capitalizeWordsWithUnderscore(i.name())))
                .toList();

        List<EnumFilterRes> invoiceTypeFilters = Arrays.stream(InvoiceType.values())
            .map(i -> new EnumFilterRes(i.name(),
                    NameUtils.capitalizeWordsWithUnderscore(i.name())))
            .toList();

        List<FloorsFilterRes> floorsFilters = allFloors.stream()
                .map(i -> new FloorsFilterRes(i.getFloorId(), i.getFloorName()))
                .toList();

        List<EnumFilterRes> groupByFilters = Arrays.stream(GroupByEnum.values())
                .map(i -> new EnumFilterRes(i.name(), i.getLabel()))
                .toList();

        InvConsolidatedFiltersRes filtersRes = new InvConsolidatedFiltersRes(createdByFilters, periodFilters,
                paymentStatusFilters, invoiceModeFilters, invoiceTypeFilters, floorsFilters, groupByFilters);

        PaginationDataRes paginationData = isPaginated ?
                new PaginationDataRes(page + 1, size, 0, 1)
                : null;

        List<Object> columnData = new ArrayList<>();

        InvConsolidatedPdfRes response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds,
                includeSubTotals, includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo,
                List.of(), List.of(), filtersRes, paginationData, activeColumnsRes, columnNames, columnData);

        if (GroupByEnum.FLOOR_ROOM_BED.equals(groupByEnum)) {

            List<InvFloorsRes> floorResponses = new ArrayList<>();

            if (floors == null){
                floors = new ArrayList<>();
            }

            for (Floors floor : floors) {

                List<InvRoomRes> roomResponses = new ArrayList<>();

                for (Rooms room : roomsByFloor.getOrDefault(floor.getFloorId(), List.of())) {

                    List<InvBedRes> bedResponses = buildBedResponses(bedsByRoom, customerByBed,
                            customersMap, invoicesByCustomer, room, includeEmptyBeds);

                    if (!includeEmptyBeds && bedResponses.isEmpty()) {
                        continue;
                    }

                    roomResponses.add(new InvRoomRes(room.getRoomId(), room.getRoomName(), floor.getFloorId(),
                            floor.getFloorName(), bedResponses));
                }

                if (!includeEmptyBeds && roomResponses.isEmpty()) {
                    continue;
                }

                floorResponses.add(new InvFloorsRes(floor.getFloorId(), floor.getFloorName(), roomResponses));
            }

            long totalElements = floorResponses.size();

            List<InvFloorsRes> paginatedFloors = paginateList(
                    floorResponses, isPaginated, page, size
            );

            paginationData = isPaginated ?
                    new PaginationDataRes(page + 1, size, totalElements,
                            (int) Math.ceil((double) totalElements / size))
                    : null;

            for (InvFloorsRes floor : paginatedFloors) {
                for (InvRoomRes room : floor.rooms()) {
                    for (InvBedRes bed : room.beds()) {
                        columnData.add(buildColumnData(bed, floor.floorName(),
                                room.roomName(), columnNames));
                    }
                }
            }

            response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds, includeSubTotals,
                    includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo, paginatedFloors,
                    List.of(), filtersRes, paginationData, activeColumnsRes, columnNames, columnData);
        }
        else if (GroupByEnum.ROOM_BED.equals(groupByEnum)) {

            List<InvRoomRes> roomResponses = new ArrayList<>();

            if (rooms == null){
                rooms = new ArrayList<>();
            }

            for (Rooms room : rooms) {

                List<InvBedRes> bedResponses = buildBedResponses(bedsByRoom, customerByBed,
                        customersMap, invoicesByCustomer, room, includeEmptyBeds);

                if (!includeEmptyBeds && bedResponses.isEmpty()) {
                    continue;
                }

                Floors floor = allFloorsMap.getOrDefault(room.getFloorId(), null);
                String floorName = null;
                if (floor != null) {
                    floorName = floor.getFloorName();
                }

                roomResponses.add(new InvRoomRes(room.getRoomId(), room.getRoomName(), room.getFloorId(),
                        floorName, bedResponses));
            }

            long totalElements = roomResponses.size();

            List<InvRoomRes> paginatedRooms = paginateList(
                    roomResponses, isPaginated, page, size
            );

            paginationData = isPaginated ?
                    new PaginationDataRes(page + 1, size, totalElements,
                    (int) Math.ceil((double) totalElements / size))
                    : null;

            for (InvRoomRes room : paginatedRooms) {
                for (InvBedRes bed : room.beds()) {
                    columnData.add(buildColumnData(bed, room.floorName(),
                            room.roomName(), columnNames));
                }
            }

            response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds, includeSubTotals,
                    includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo, List.of(),
                    paginatedRooms, filtersRes, paginationData, activeColumnsRes, columnNames, columnData);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private InvConsolidatedRes toInvConsolidatedRes(InvoicesV1 invoice, Customers customer) {

        if (invoice == null){
            return null;
        }

        String name = "NA";
        String invoiceType = "NA";
        String dueAmount = "NA";
        String paymentStatus = "NA";

        if (customer != null){
            name = NameUtils.getFullName(customer.getFirstName(), customer.getLastName());
        }

        if (InvoiceType.RENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Rent";
        } else if (InvoiceType.REASSIGN_RENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Re-Assign Rent";
        } else if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Booking";
        } else if (InvoiceType.ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Advance";
        } else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Additional Advance";
        } else if (InvoiceType.SETTLEMENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Settlement";
        } else if (InvoiceType.EB.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "EB";
        } else if (InvoiceType.OTHER.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Other";
        } else if (InvoiceType.AMOUNT_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                InvoiceType.EB_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Retainer";
        } else {
            invoiceType = Utils.capitalize(invoice.getInvoiceType());
        }

        if (PaymentStatus.PAID.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Paid";
        } else if (PaymentStatus.PARTIAL_PAYMENT.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Partially Paid";
        } else if (PaymentStatus.REFUNDED.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Refunded";
        } else if (PaymentStatus.PARTIAL_REFUND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Partially Refunded";
        } else if (PaymentStatus.PENDING.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Pending";
        } else if (PaymentStatus.PENDING_REFUND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Pending Refunded";
        } else if (PaymentStatus.CANCELLED.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Cancelled";
        } else if (PaymentStatus.ADVANCE_IN_HAND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Advance In Hand";
        } else {
            paymentStatus = Utils.capitalize(invoice.getPaymentStatus());
        }

        if (invoice.isCancelled()) {
            paymentStatus = "Cancelled";
        }

        double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
        double paidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0;
        double pendingAmount = totalAmount - paidAmount;

        if (!InvoiceType.SETTLEMENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            dueAmount = String.valueOf(pendingAmount);
        }
        else {
            if (totalAmount < 0){
                if (invoice.getPaidAmount() != null){
                    double amount = totalAmount + paidAmount;
                    dueAmount = String.valueOf(amount);
                } else {
                    dueAmount = String.valueOf(totalAmount);
                }
            } else {
                dueAmount = String.valueOf(pendingAmount);
            }
        }

        return new InvConsolidatedRes(invoice.getInvoiceNumber(), name, invoiceType,
                Utils.dateToString(invoice.getInvoiceStartDate()), Utils.dateToString(invoice.getInvoiceDueDate()),
                String.valueOf(invoice.getTotalAmount()), String.valueOf(paidAmount),
                dueAmount, paymentStatus, invoice.getInvoiceUrl()
        );
    }

    private List<InvBedRes> buildBedResponses(Map<Integer, List<Beds>> bedsByRoom,
                                              Map<Integer, String> customerByBed,
                                              Map<String, Customers> customersMap,
                                              Map<String, List<InvoicesV1>> invoicesByCustomer,
                                              Rooms room, boolean includeEmptyBeds) {

        List<InvBedRes> bedResponses = new ArrayList<>();

        for (Beds bed : bedsByRoom.getOrDefault(room.getRoomId(), List.of())) {

            String customerId = customerByBed.get(bed.getBedId());

            Customers customer = customersMap.getOrDefault(customerId, null);

            List<InvoicesV1> customerInvoices = invoicesByCustomer.getOrDefault(customerId, List.of());

            List<InvConsolidatedRes> invoiceResponses = customerInvoices
                    .stream()
                    .map(i -> toInvConsolidatedRes(i, customer))
                    .toList();

            if (!includeEmptyBeds && invoiceResponses.isEmpty()) {
                continue;
            }

            int invoiceCount = customerInvoices.size();

            double actualRent = 0;
            double pendingRent = 0;
            double ebAmount = 0;
            double pendingAdvance = 0;
            double pendingNonRefundable = 0;
            double totalPendingAmount = 0;

            for (InvoicesV1 invoice : customerInvoices) {

                double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
                double invPaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0;
                double pendingAmount = totalAmount - invPaidAmount;
                double balanceAmount = invoice.getBalanceAmount() != null ? invoice.getBalanceAmount() : 0;
                double deductionAmount = invoice.getDeductionAmount() != null ? invoice.getDeductionAmount() : 0;

                List<com.smartstay.reports.dao.InvoiceItems> invoiceItems = invoice.getInvoiceItems();

                if (InvoiceType.RENT.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.REASSIGN_RENT.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.OTHER.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.EB.name().equalsIgnoreCase(invoice.getInvoiceType())){
                    actualRent += totalAmount;
                    pendingRent += pendingAmount;

                    if (invoiceItems != null && !invoiceItems.isEmpty()){
                        ebAmount = invoiceItems
                                .stream()
                                .filter(item ->  com.smartstay.reports.ennum.InvoiceItems.EB.name()
                                        .equals(item.getInvoiceItem()))
                                .mapToDouble(com.smartstay.reports.dao.InvoiceItems::getAmount)
                                .sum();
                    }
                }

                if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.AMOUNT_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.EB_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
                    pendingAdvance += balanceAmount;
                }

                pendingNonRefundable += deductionAmount;
            }

            totalPendingAmount = pendingAdvance - pendingNonRefundable - pendingRent;

            String tenantName = null;
            String countryCode = null;
            String mobile = null;
            if (customer != null){
                tenantName = NameUtils.getFullName(customer.getFirstName(), customer.getLastName());
                countryCode = "+" + String.valueOf(customer.getCountryCode());
                mobile = customer.getMobile();
            }

            bedResponses.add(new InvBedRes(bed.getBedId(), bed.getBedName(), tenantName, countryCode,
                    mobile, String.valueOf(actualRent), String.valueOf(pendingRent), String.valueOf(ebAmount),
                    String.valueOf(pendingAdvance), String.valueOf(pendingNonRefundable),
                    String.valueOf(totalPendingAmount), String.valueOf(invoiceCount), invoiceResponses));
        }

        return bedResponses;
    }

    private <T> List<T> paginateList(List<T> data, boolean isPaginated,
                                     int page, int size) {

        if (!isPaginated) {
            return data;
        }

        int fromIndex = page * size;

        if (fromIndex >= data.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, data.size());

        return data.subList(fromIndex, toIndex);
    }

    private List<Object> buildColumnData(InvBedRes bedRes, String floorName, String roomName,
                                         List<String> columnNames) {

        List<Object> row = new ArrayList<>();

        for (String column : columnNames) {
            switch (column) {
                case InvRegisterConsolidatedColumnUtils.FLOOR ->
                        row.add(floorName);
                case InvRegisterConsolidatedColumnUtils.ROOM ->
                        row.add(roomName);
                case InvRegisterConsolidatedColumnUtils.BED ->
                        row.add(bedRes.bedName());
                case InvRegisterConsolidatedColumnUtils.TENANT_NAME ->
                        row.add(bedRes.tenantName());
                case InvRegisterConsolidatedColumnUtils.CONTACT_NUMBER ->
                        row.add(bedRes.tenantCountryCode() + " " + bedRes.tenantMobile());
                case InvRegisterConsolidatedColumnUtils.ACTUAL_RENT ->
                        row.add(bedRes.actualRent());
                case InvRegisterConsolidatedColumnUtils.PENDING_RENT ->
                        row.add(bedRes.pendingRent());
                case InvRegisterConsolidatedColumnUtils.EB_AMOUNT ->
                        row.add(bedRes.ebAmount());
                case InvRegisterConsolidatedColumnUtils.PENDING_ADVANCE ->
                        row.add(bedRes.pendingAdvance());
                case InvRegisterConsolidatedColumnUtils.PENDING_NON_REFUNDABLE ->
                        row.add(bedRes.pendingNonRefundable());
                case InvRegisterConsolidatedColumnUtils.TOTAL_PENDING_AMOUNT ->
                        row.add(bedRes.totalPendingAmount());
                case InvRegisterConsolidatedColumnUtils.INVOICES ->
                        row.add(bedRes.invoiceCount());
            }
        }

        row.add(bedRes.invoices());

        return row;
    }
}
