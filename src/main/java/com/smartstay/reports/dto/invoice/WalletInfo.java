package com.smartstay.reports.dto.invoice;

import java.util.List;

public record WalletInfo(Integer noOfItems,
                         Double totalWalletAmount,
                         List<WalletItems> walletItems) {
}
