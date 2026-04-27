package com.rs.subscription.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitApprovalRequest {
    private Long approvalRequestId;
    private String submittedBy;
    // Override giá trị hợp đồng nếu cần tính lại cấp duyệt
    private BigDecimal contractValue;
}
