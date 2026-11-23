package com.smartstay.reports.service;

import com.smartstay.reports.dao.ColumnFilters;
import com.smartstay.reports.dao.TableColumns;
import com.smartstay.reports.repositories.TableColumnsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TableColumnService {

    @Autowired
    private TableColumnsRepository tableColumnsRepository;
    @Autowired
    private FilterOptionsService filterOptionsService;

    public List<ColumnFilters> getInvRegConsolidatedColumns(String hostelId, String userId,
                                                            String moduleName){

        TableColumns tableColumns = tableColumnsRepository
                .findByHostelIdAndUserId(hostelId, userId, moduleName);

        if (tableColumns == null) {
            return filterOptionsService.findInvRegisterConsolidatedBasicFilters();
        }

        return tableColumns.getColumns()
                .stream()
                .sorted(Comparator.comparing(ColumnFilters::getOrder))
                .toList();
    }
}
