package com.smartstay.reports.service;

import com.smartstay.reports.dao.Credentials;
import com.smartstay.reports.repositories.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    public Credentials getByService(String service){
        return credentialsRepository.findByService(service);
    }
}
