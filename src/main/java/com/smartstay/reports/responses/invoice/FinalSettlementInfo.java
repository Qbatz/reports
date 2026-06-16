package com.smartstay.reports.responses.invoice;

import com.smartstay.reports.dto.customer.StayInfo;
import com.smartstay.reports.dto.invoice.*;
import com.smartstay.reports.dto.settlement.FinalSettlementInvoiceInfo;
import com.smartstay.reports.responses.customers.CustomerInfo;
import com.smartstay.reports.responses.hostel.TemplateInfo;

public record FinalSettlementInfo(FinalSettlementHeaderInfo headerInfo,
                                  StayInfo stayInfo,
                                  AccountDetails accountDetails,
                                  TemplateInfo configInfo,
                                  CustomerInfo customerInfo,
                                  UnpaidInvoiceInfo unpaidInvoiceInfo,
                                  DeductionsInfo deductionsInfo,
                                  AdvanceItems advanceItems,
                                  AdvanceItems bookingItems,
                                  CurrentRentInfo currentMonthRentInfo,
                                  CurrentMonthEbInfo currentMonthEbInfo,
                                  WalletInfo walletInfo,
                                  FinalSettlementInvoiceInfo invoiceInfo) {
}
