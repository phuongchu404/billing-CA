package com.rs.subscription.enums;

import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;

import java.util.Arrays;

public final class CommercialEnums {

    private CommercialEnums() {
    }

    public enum RequestType {
        CREATE_PLAN_TEMPLATE,
        REQUEST_GROUP_PLAN_ASSIGNMENT,
        REQUEST_RETAIL_PLAN_SCHEDULE,
        CANCEL_SUBSCRIPTION,
        SUSPEND_SUBSCRIPTION
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        DENIED
    }

    // Multi-level approval: trạng thái của toàn bộ request
    public enum MultiApprovalRequestStatus {
        DRAFT,
        IN_APPROVAL,
        NEED_REVISION,
        APPROVED,
        REJECTED
    }

    // Multi-level approval: trạng thái của từng step
    public enum ApprovalStepStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SKIPPED
    }

    // Role cấp duyệt
    public enum ApprovalLevel {
        LEVEL_1,  // Trưởng phòng
        LEVEL_2,  // Giám đốc
        LEVEL_3   // CFO
    }

    public enum AssignmentType {
        GROUP_PLAN,
        RETAIL_PLAN
    }

    public enum AuditAction {
        REQUEST,
        APPROVE,
        REJECT,
        ACTIVATE,
        STOP,
        EXPIRE
    }

    public enum CustomerSegment {
        GROUP,
        INDIVIDUAL
    }

    public enum TemplateScope {
        PUBLIC,
        PARTNER_PRIVATE,
        SYSTEM
    }

    public enum TemplateStatus {
        DRAFT,
        AVAILABLE,
        INACTIVE,
        ARCHIVED
    }

    public enum SubjectType {
        INDIVIDUAL,
        ORGANIZATION,
        INDIVIDUAL_OF_ORG
    }

    public enum ValidityUnit {
        DAY,
        MONTH,
        YEAR
    }

    public enum PricingMetric {
        SIGNING_COUNT,
        CERTIFICATE_COUNT
    }

    public enum ContactType {
        CONTRACT,
        BILLING,
        SUPPORT,
        PIC
    }

    public enum GroupStatus {
        ACTIVE,
        INACTIVE
    }

    public enum MemberRole {
        OPERATOR,
        MEMBER
    }

    public enum AssignmentStatus {
        AVAILABLE,
        REQUESTED,
        APPROVED,
        ACTIVE,
        REJECTED,
        STOPPED,
        EXPIRED
    }

    public enum ScheduleStatus {
        AVAILABLE,
        REQUESTED,
        APPROVED,
        ACTIVE,
        INACTIVE
    }

    public enum StatementStatus {
        DRAFT,
        FINALIZED,
        EXPORTED
    }

    public enum SubscriberType {
        INDIVIDUAL,
        GROUP
    }

    public enum SubscriptionStatus {
        PENDING,
        ACTIVE,
        EXPIRED,
        CANCELLED,
        SUSPENDED
    }

    public enum SubscriptionSourceType {
        RETAIL_PURCHASE,
        GROUP_ASSIGNMENT,
        MANUAL
    }

    public enum AggregateScope {
        GROUP_ASSIGNMENT,
        RETAIL_PLAN,
        GROUP,
        USER
    }

    public enum PeriodType {
        DAY,
        MONTH
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        REFUNDED
    }

    public enum PaymentScope {
        SUBSCRIPTION,
        GROUP_ASSIGNMENT,
        SETTLEMENT
    }

    public enum UsageType {
        SIGNING,
        CERTIFICATE_CREATED,
        CERTIFICATE_RENEWED,
        CERTIFICATE_REVOKED
    }

    public enum ProvisioningStatus {
        PENDING,
        COMPLETED,
        FAILED,
        FAILED_PERMANENT
    }

    public static <E extends Enum<E>> String normalize(String rawValue, Class<E> enumClass, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, fieldName + " is required", 400);
        }
        try {
            return Enum.valueOf(enumClass, rawValue.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new SmsException(
                ErrorCodes.VALIDATION_FAILED,
                "Invalid " + fieldName + ": " + rawValue + ". Allowed values: " + Arrays.toString(enumClass.getEnumConstants()),
                400
            );
        }
    }
}
