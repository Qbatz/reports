package com.smartstay.reports.config;

import com.smartstay.reports.dao.Credentials;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ServicePrinciple implements UserDetails {

    private Credentials credential;

    public ServicePrinciple(Credentials credential) {
        this.credential = credential;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return credential.getSecretValue();
    }

    @Override
    public String getUsername() {
        return credential.getService();
    }
}
