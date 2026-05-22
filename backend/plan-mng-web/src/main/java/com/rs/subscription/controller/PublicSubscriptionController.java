package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.ExecuteRetailPlanFlowRequest;
import com.rs.subscription.dto.response.CommercialFlowResponse;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.service.CommercialOrchestrationService;
import com.rs.subscription.service.RuntimeSubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/subscriptions")
@RequiredArgsConstructor
public class PublicSubscriptionController {

    private final CommercialOrchestrationService commercialOrchestrationService;
    private final RuntimeSubscriptionService runtimeSubscriptionService;

    /**
     * Khách hàng cá nhân đăng ký gói sau khi thanh toán thành công.
     * Schedule phải ở trạng thái ACTIVE — do admin đã kích hoạt trước.
     */
    @PostMapping
    public ApiResponse<RuntimeSubscriptionResponse> subscribe(@Valid @RequestBody PublicSubscribeRequest body) {
        ExecuteRetailPlanFlowRequest req = new ExecuteRetailPlanFlowRequest();
        req.setUserId(body.getUserId());
        req.setPricingRuleId(body.getPricingRuleId());
        req.setPaymentReference(body.getPaymentReference());
        req.setActor("USER:" + body.getUserId());
        req.setApproveNow(false);
        req.setActivateNow(false);
        req.setIssueSubscription(true);
        req.setSubscriptionStatus(CommercialEnums.SubscriptionStatus.ACTIVE.name());

        CommercialFlowResponse flow = commercialOrchestrationService.executeRetailPlanFlow(body.getScheduleId(), req);
        return ApiResponse.success(flow.getSubscription(), "Subscription created successfully");
    }

    /**
     * Lấy danh sách subscription của 1 user cụ thể.
     */
    @GetMapping
    public ApiResponse<List<RuntimeSubscriptionResponse>> listByUser(@RequestParam @NotNull @Positive Long userId) {
        return ApiResponse.success(runtimeSubscriptionService.listByUserId(userId), "Fetched subscriptions");
    }

    @Data
    public static class PublicSubscribeRequest {
        @NotNull
        @Positive
        private Long userId;

        @NotNull
        @Positive
        private Long scheduleId;

        private Long pricingRuleId;
        private String paymentReference;
    }
}
