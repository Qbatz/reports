package com.smartstay.reports.utils;

import com.smartstay.reports.dao.BankingV1;
import com.smartstay.reports.dao.BankingV2;
import com.smartstay.reports.ennum.BankAccountType;

public class BankingUtils {

    public static String getPaymentModeWithHolder(BankingV2 bankingV2) {

        if (bankingV2 == null){
            return null;
        }

        StringBuilder builder = new StringBuilder();

        if (bankingV2.getAccountHolderName() != null && !bankingV2.getAccountHolderName().isBlank()){
            builder.append(bankingV2.getAccountHolderName());
        }

        if (bankingV2.getAccountType() != null && !bankingV2.getAccountType().isBlank()){

            if (!builder.toString().isBlank()){
                builder.append("-");
            }

            if (BankAccountType.CASH.name().equalsIgnoreCase(bankingV2.getAccountType())){

                builder.append("Cash");

            } else if (BankAccountType.CARD.name().equalsIgnoreCase(bankingV2.getAccountType())) {

                builder.append("Card");

            } else if (BankAccountType.BANK.name().equalsIgnoreCase(bankingV2.getAccountType())) {

                String bankName = bankingV2.getBankName() != null && !bankingV2.getBankName().isBlank()
                        ? Utils.capitalize(bankingV2.getBankName()) : "Bank";

                builder.append(bankName);

            } else if (BankAccountType.UPI.name().equalsIgnoreCase(bankingV2.getAccountType())) {

                builder.append("Upi");

            } else {

                String accountType = bankingV2.getAccountType() != null && !bankingV2.getAccountType().isBlank()
                        ? Utils.capitalize(bankingV2.getAccountType()) : "Unknown";

                builder.append(accountType);
            }
        }

        if (builder.toString().isBlank()){
            return null;
        }

        return builder.toString();
    }

    public static String getPaymentModeWithHolder(BankingV1 bankingV1) {
        StringBuilder bank = new StringBuilder();
        if (bankingV1 != null) {
            bank.append(bankingV1.getAccountHolderName());
            if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                bank.append("-");
                bank.append("Cash");
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                if (bankingV1.getAccountHolderName() != null) {

                    bank.append("-");
                    bank.append("Card");
                }
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                if (bankingV1.getAccountHolderName() != null) {
                    bank.append("-");
                    bank.append(bankingV1.getBankName());
                }
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
                if (bankingV1.getAccountHolderName() != null) {
                    bank.append("-");
                    bank.append("Upi");
                }
            }
        }

        return bank.toString();
    }
}
