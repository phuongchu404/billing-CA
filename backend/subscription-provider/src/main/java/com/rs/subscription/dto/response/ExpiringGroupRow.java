package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class ExpiringGroupRow {
    private Long groupId;
    private String code;
    private String name;
    private String plan;
    private String expiry;
}
