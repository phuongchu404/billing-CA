package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request tạo/cập nhật Group từ giao diện plan management.
 * picEmails: danh sách email nhân sự phụ trách (contactType=PIC)
 * contactEmails: danh sách email đại diện đại lý (contactType=CONTRACT)
 */
@Data
public class UpsertGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 200)
    private String groupName;

    /** List email nhân sự phụ trách */
    private List<String> picEmails;

    /** List email người đại diện đại lý */
    private List<String> contactEmails;

    /** Mã hợp đồng tham chiếu (không bắt buộc) */
    @Size(max = 200)
    private String refContractNo;

    /** Người tạo — nếu null thì mặc định "system" */
    @Size(max = 100)
    private String createdBy;

    /** userId của nhân viên nội bộ phụ trách group này (nullable) */
    @Size(max = 36)
    private String ownerUserId;
}
