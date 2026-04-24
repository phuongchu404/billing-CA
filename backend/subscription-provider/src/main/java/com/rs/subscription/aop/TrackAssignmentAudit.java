package com.rs.subscription.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackAssignmentAudit {
    String assignmentType();
    String entityId();
    String action();
    String actor() default "";
    String note() default "";
}
