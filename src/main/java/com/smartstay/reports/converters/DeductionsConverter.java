package com.smartstay.reports.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.reports.dto.customer.Deductions;
import jakarta.persistence.AttributeConverter;

import java.util.List;

public class DeductionsConverter implements AttributeConverter<List<Deductions>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Deductions> deductions) {
        try {
            return objectMapper.writeValueAsString(deductions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<Deductions> convertToEntityAttribute(String s) {
        try {
            return objectMapper.readValue(s, new TypeReference<List<Deductions>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
