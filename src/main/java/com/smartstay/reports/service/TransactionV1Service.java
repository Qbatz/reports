package com.smartstay.reports.service;

import com.smartstay.reports.dao.InvoicesV1;
import com.smartstay.reports.dao.TransactionV1;
import com.smartstay.reports.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TransactionV1Service {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private InvoiceService invoiceService;

    public ResponseEntity<?> getReceiptPDF(String hostelId, String transactionId) {
        TransactionV1 transactionV1 = transactionRepository.findByHostelIdAndTransactionId(hostelId, transactionId);
        if (transactionV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 invoicesV1 = invoiceService.getInvoice(transactionV1.getInvoiceId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return null;
    }


}
