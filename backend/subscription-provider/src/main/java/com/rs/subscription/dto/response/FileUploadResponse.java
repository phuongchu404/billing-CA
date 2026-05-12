package com.rs.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {
    private String bucket;
    private String objectName;
    /** "{bucket}/{objectName}" — value stored in DB */
    private String storagePath;
    /** Full public URL for immediate display in browser */
    private String url;
    private String contentType;
    private long size;
}
