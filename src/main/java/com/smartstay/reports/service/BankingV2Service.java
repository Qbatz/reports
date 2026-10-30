package com.smartstay.reports.service;

import com.smartstay.reports.dao.BankingMethods;
import com.smartstay.reports.dao.BankingV2;
import com.smartstay.reports.dto.invoice.AccountDetails;
import com.smartstay.reports.ennum.BankAccountType;
import com.smartstay.reports.ennum.PaymentMethod;
import com.smartstay.reports.repositories.BankingV2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BankingV2Service {

    @Autowired
    private BankingV2Repository bankingV2Repository;

    public List<BankingV2> getByBankIds(Set<String> bankIds) {
        return bankingV2Repository
                .findAllByBankIdInAndIsActiveTrueAndIsDeletedFalse(bankIds);
    }

    public AccountDetails getBankAccountDetails(String bankId, String qrCode) {

        AccountDetails accountDetails = new AccountDetails(null, null,
                null, null, qrCode);

        BankingV2 bankingV2 = bankingV2Repository
                .findByBankIdAndIsActiveTrueAndIsDeletedFalse(bankId);

        if (bankingV2 != null) {

            List<BankingMethods> bankingMethods = bankingV2.getBankingMethods();

            String upiId = null;
            if (bankingMethods != null) {
                BankingMethods upiMethod = bankingMethods.stream()
                        .filter(i -> PaymentMethod.UPI.name()
                                .equalsIgnoreCase(i.getPaymentMethod()))
                        .findFirst()
                        .orElse(null);

                if (upiMethod != null) {
                    upiId = upiMethod.getUpiId();
                }
            }

            accountDetails = new AccountDetails(bankingV2.getAccountNumber(), bankingV2.getIfscCode(),
                    bankingV2.getBankName(), upiId, qrCode);
        }

        return accountDetails;
    }

    public String getPaymentMode(String bankId){

        BankingV2 bankingV2 = bankingV2Repository
                .findByBankIdAndIsActiveTrueAndIsDeletedFalse(bankId);

        if (bankingV2 == null){
            return null;
        }

        StringBuilder builder = new StringBuilder();

        if (bankingV2.getAccountHolderName() != null && !bankingV2.getAccountHolderName().isBlank()){
            builder.append(bankingV2.getAccountHolderName());
        }

        if (bankingV2.getAccountType() != null && !bankingV2.getAccountType().isBlank()){

            if (BankAccountType.BANK.name().equalsIgnoreCase(bankingV2.getAccountType())){

                if (!builder.toString().isBlank()){
                    builder.append("-");
                }
                builder.append(bankingV2.getBankName() != null && !bankingV2.getBankName().isBlank()
                        ? bankingV2.getBankName() : "Bank");
            }

            if (!builder.toString().isBlank()){
                builder.append("-");
            }

            builder.append(bankingV2.getAccountType());
        }

        return builder.toString();
    }

    public List<String> getBankIdsByAccountTypes(String hostelId, List<String> accountTypes) {
        return bankingV2Repository.findBankIdsByAccountTypes(hostelId, accountTypes);
    }
}
