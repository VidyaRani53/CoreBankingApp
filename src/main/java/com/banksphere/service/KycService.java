package com.banksphere.service;

import com.banksphere.dto.kyc.KycResponse;
import com.banksphere.dto.kyc.KycReviewRequest;
import com.banksphere.dto.kyc.KycSubmitRequest;

import java.util.List;
import java.util.UUID;

public interface KycService {
    KycResponse submit(KycSubmitRequest request);
    List<KycResponse> myHistory();
    List<KycResponse> inbox();
    KycResponse review(Long kycRequestId, KycReviewRequest request);
}