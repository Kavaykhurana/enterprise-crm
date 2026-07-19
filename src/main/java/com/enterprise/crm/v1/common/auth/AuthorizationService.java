package com.enterprise.crm.v1.common.auth;

import com.enterprise.crm.v1.user.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    public void verifyRecordAccess(UUID assignedSalesRepId) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        // Admins and Sales Managers bypass ownership checks
        if (currentUser.getRole().equals("ADMIN") || currentUser.getRole().equals("SALES_MANAGER")) {
            return;
        }

        if (currentUser.getRole().equals("SALES_EXECUTIVE")) {
            if (assignedSalesRepId == null || !assignedSalesRepId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied: You are not authorized to access this record.");
            }
        } else {
            throw new AccessDeniedException("Access denied: Unknown role " + currentUser.getRole());
        }
    }
}
