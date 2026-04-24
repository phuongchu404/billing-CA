package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class PermissionResponse {
    private Long permissionId;
    private String permissionKey;
    private String displayName;
    private String moduleGroup;
    private String groupName;
    private Integer sortOrder;
}
