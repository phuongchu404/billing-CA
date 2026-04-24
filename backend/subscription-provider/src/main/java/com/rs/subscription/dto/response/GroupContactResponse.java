package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class GroupContactResponse {
    private Long groupContactId;
    private Long groupId;
    private String contactType;
    private String email;
    private String fullName;
    private String phone;
    private Boolean isPrimary;
    private Boolean receiveUsageAlert;
    private Boolean isActive;
}
