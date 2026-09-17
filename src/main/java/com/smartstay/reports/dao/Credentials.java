package com.smartstay.reports.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Credentials {
    @Id
    String service;
    String clientId;
    String authToken;
    String secretValue;
    String refreshToken;
    String otherSecrets;
}
