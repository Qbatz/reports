package com.smartstay.reports.service;

import com.smartstay.reports.dao.Users;
import com.smartstay.reports.repositories.UserRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    @Autowired
    private UserRepositories userRepositories;
    public List<Users> findByUserIds(List<String> userIds) {
        return userRepositories.findAllById(userIds);
    }
}
