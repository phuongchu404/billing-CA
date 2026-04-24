package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class SystemSettingResponse {
    private String key;
    private String value;
    private String type;
    private String category;
    private String description;
}
