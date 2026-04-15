package com.banksphere.service;

import com.banksphere.dto.customer.CustomerProfileUpdateRequest;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.entity.CustomerUpdateRequest;

import java.util.List;

public interface CustomerService {
    CustomerResponse getMyProfile();
    public void requestProfileUpdate(CustomerProfileUpdateRequest request);

}