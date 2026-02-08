package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.InvoicesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String>  {
    InvoicesV1 findByHostelIdAndInvoiceId(String hostelId, String invoiceId);
}
