package com.banksphere.service;

import com.banksphere.dto.account.*;

import java.util.List;
import java.util.UUID;

public interface AccountApplicationService {
    AccountApplicationResponse submit(AccountApplicationSubmitRequest request);
    List<AccountApplicationResponse> myApplications();
    List<AccountApplicationResponse> pendingForMyBranch();
    AccountResponse approve(UUID applicationId, ApplicationReviewRequest request);
    AccountApplicationResponse reject(UUID applicationId, ApplicationReviewRequest request);
}