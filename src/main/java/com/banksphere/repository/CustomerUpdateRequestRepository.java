package com.banksphere.repository;


import com.banksphere.entity.CustomerUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerUpdateRequestRepository
        extends JpaRepository<CustomerUpdateRequest, Long> {

    List<CustomerUpdateRequest> findByStatus(String status);
}

