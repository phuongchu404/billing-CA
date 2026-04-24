package com.rs.subscription.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackSubscriptionAudit {
    String subscriptionId();
    String actor() default "";
    String reason() default "";
    String sourceType() default "";
    boolean resolveActorFromUserId() default false;
}
