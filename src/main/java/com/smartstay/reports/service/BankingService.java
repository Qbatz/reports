package com.smartstay.reports.service;

import com.smartstay.reports.dao.BankingV1;
import com.smartstay.reports.ennum.BankAccountType;
import com.smartstay.reports.repositories.BankingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankingService {
    @Autowired
    private BankingRepository bankingRepository;

    public String getPaymentMode(String bankId) {
        try {
            StringBuilder bankName = new StringBuilder();
            BankingV1 bankingV1 = bankingRepository.getReferenceById(bankId);
            if (bankingV1.getBankId() == null) {
                return null;
            }

            bankName.append(bankingV1.getAccountHolderName());
            bankName.append("-");
            if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                if (bankingV1.getBankName() != null) {
                    bankName.append(" ");
                    bankName.append(bankingV1.getBankName());
                    bankName.append(" ");
                }
            }
            bankName.append(bankingV1.getAccountType());

            return bankName.toString();
        }
        catch (EntityNotFoundException nnf) {
            return null;
        }
    }

    public List<BankingV1> findByBankIds(List<String> bankIds) {
        return bankingRepository.findAllById(bankIds);
    }
}
