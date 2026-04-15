package com.banksphere.service;

import com.banksphere.entity.CustomerUpdateRequest;

import java.util.List;

public interface CSRService {
    public void approveUpdate(Long requestId);
    public void rejectUpdate(Long requestId, String reason);
    public List<CustomerUpdateRequest> getPendingRequests();
}
