package com.rs.subscription.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RolePermissionMatrixResponse {
    private List<RoleResponse> roles;
    private List<ModuleGroupResponse> moduleGroups;
    /** roleId -> list of permissionIds */
    private Map<Long, List<Long>> rolePermissions;

    @Data
    public static class ModuleGroupResponse {
        private String moduleName;
        private List<PermissionGroupResponse> permissionGroups;
    }

    @Data
    public static class PermissionGroupResponse {
        private String groupName;
        private List<PermissionResponse> permissions;
    }
}
