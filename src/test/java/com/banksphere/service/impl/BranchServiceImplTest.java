package com.banksphere.service.impl;

import com.banksphere.dto.branch.CreateBranchRequest;
import com.banksphere.entity.Branch;
import com.banksphere.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BranchServiceImplTest {

    private BranchRepository branchRepository;
    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        branchRepository = mock(BranchRepository.class);
        branchService = new BranchServiceImpl(branchRepository);
    }

    @Test
    void createBranch_shouldThrow_whenBranchCodeExists() {
        CreateBranchRequest req = new CreateBranchRequest();
        req.setBranchCode("HYD001");
        req.setName("Hyderabad");
        req.setIfsc("BANK0001");
        req.setAddressLine1("Addr");
        req.setCity("Hyd");
        req.setState("TS");
        req.setPincode("500001");

        when(branchRepository.existsByBranchCode("HYD001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> branchService.createBranch(req));

        assertTrue(ex.getMessage().contains("Branch code already exists"));
        verify(branchRepository, never()).save(any());
    }

    @Test
    void updateBranchStatus_shouldUpdateToInactive() {
        UUID id = UUID.randomUUID();

        Branch existing = Branch.builder()
                .id(id)
                .branchCode("HYD001")
                .name("Hyderabad")
                .status("ACTIVE")
                .build();

        when(branchRepository.findById(id)).thenReturn(Optional.of(existing));
        when(branchRepository.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = branchService.updateBranchStatus(id.toString(), "INACTIVE");

        assertEquals("INACTIVE", resp.getStatus());

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertEquals("INACTIVE", captor.getValue().getStatus());
    }
}