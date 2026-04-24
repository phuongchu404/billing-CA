package com.rs.subscription.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePlanRequest {
    @NotBlank(message = "Plan code is required")
    @Size(max = 50)
    private String planCode;

    @NotBlank(message = "Plan name is required")
    @Size(max = 150)
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be 0 or greater")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    /** VALIDITY_PERIOD | SIGNING_COUNT | COMBINED. Defaults to VALIDITY_PERIOD. */
    private String planType = "VALIDITY_PERIOD";

    @NotNull(message = "Validity days is required")
    @Min(value = 1, message = "Validity days must be at least 1")
    private Integer validityDays;

    /** Original user-entered amount (for display). */
    private Integer validityAmount;

    /** Original user-selected unit (for display). One of: DAYS, MONTHS, YEARS. */
    private String validityUnit;

    /** Required for COMBINED plan type. Signing operations allowed before expiry. */
    @Min(value = 1, message = "Max signing quota must be at least 1")
    private Integer maxSigningQuota;

    /** Required when isGroupPlan=true. Maximum number of members in the group. */
    @Min(value = 1, message = "Max members must be at least 1")
    private Integer maxMembers;

    private Boolean allowBulkSigning = false;
    private Boolean allowApiAccess = false;
    private Boolean isGroupPlan = false;

    /** Only meaningful when isGroupPlan=true. Defaults to GROUP_FOLLOWS. */
    private String groupMemberValidityMode = "GROUP_FOLLOWS";

    /** Optional: date from which this plan becomes available (inclusive). */
    private LocalDate effectiveFrom;

    /** Optional: date after which this plan is no longer available (inclusive). */
    private LocalDate effectiveTo;

    /** Whether the plan is visible to clients. Defaults to true. */
    private Boolean isVisible = true;
}
