package com.rs.subscription.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Published whenever an individual plan config is created, approved, or stopped.
 * Consumed by PlanSseController to push a notification to connected frontend-public clients.
 */
@Getter
public class PlanUpdatedEvent extends ApplicationEvent {

    private final Long planTemplateId;
    /** "CREATED" | "APPROVED" | "STOPPED" | "DEACTIVATED" */
    private final String action;

    public PlanUpdatedEvent(Object source, Long planTemplateId, String action) {
        super(source);
        this.planTemplateId = planTemplateId;
        this.action = action;
    }
}
