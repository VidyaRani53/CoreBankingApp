package com.banksphere.service;

import com.banksphere.dto.employee.CreateEmployeeRequest;
import com.banksphere.dto.employee.CreateEmployeeResponse;
import com.banksphere.dto.employee.EmployeeResponse;
import java.util.List;
import com.banksphere.dto.employee.ResetPasswordResponse;
public interface AdminEmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    ResetPasswordResponse resetPassword(String username);
}