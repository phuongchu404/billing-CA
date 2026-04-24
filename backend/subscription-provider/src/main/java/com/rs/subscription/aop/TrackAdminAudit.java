package com.rs.subscription.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackAdminAudit {
    String actor();
    String action();
    String entityType();
    String entityId();
    String details() default "";
    boolean resolveActorFromUserId() default false;
}
