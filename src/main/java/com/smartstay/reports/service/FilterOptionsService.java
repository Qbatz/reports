package com.smartstay.reports.service;

import com.smartstay.reports.dao.ColumnFilters;
import com.smartstay.reports.dao.FilterOptions;
import com.smartstay.reports.ennum.FilterOptionsModule;
import com.smartstay.reports.repositories.FilterOptionsRepository;
import com.smartstay.reports.utils.columnOptions.InvRegisterConsolidatedColumnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FilterOptionsService {

    @Autowired
    private FilterOptionsRepository filterOptionsRepository;

    public List<ColumnFilters> findInvRegisterConsolidatedBasicFilters(){

        FilterOptions filterOptions = filterOptionsRepository
                .findByModuleName(FilterOptionsModule.MODULE_INVOICE_REGISTER_CONSOLIDATED.name());

        if (filterOptions != null && filterOptions.getFilterOptions() != null
                && !filterOptions.getFilterOptions().isEmpty()) {
            return filterOptions.getFilterOptions()
                    .stream()
                    .sorted(Comparator.comparing(ColumnFilters::getOrder))
                    .toList();
        }

        // Fall back to the code-defined defaults so the feature works without a DB seed row.
        return InvRegisterConsolidatedColumnUtils.defaultColumns();
    }
}
