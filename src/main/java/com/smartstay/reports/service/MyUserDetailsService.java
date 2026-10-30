package com.smartstay.reports.service;

import com.smartstay.reports.config.ServicePrinciple;
import com.smartstay.reports.dao.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private CredentialsService credentialsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Credentials credential = credentialsService.getByService(username);

        if (credential == null) {
            return null;
        }

        return new ServicePrinciple(credential);
    }
}
