package com.rs.subscription.aop;

import java.lang.annotation.*;

/**
 * Place on a @Service class to auto-log all write operations to admin_audit_logs.
 * Place on a method to override class-level settings or to mark a read method for logging.
 * Place on a method with skip=true to exclude that method from class-level auditing.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Fixed action name (e.g. "RESET_PASSWORD"). Auto-derived from method name if blank. */
    String action() default "";

    /** Entity type (e.g. "USER"). Auto-derived from service class name if blank. */
    String entityType() default "";

    /** SpEL to extract entityId from args (#p0, #userId) or return value (#result). */
    String entityId() default "";

    /** SpEL to build a details string. */
    String details() default "";

    /** Set true on a method to exclude it from class-level auditing. */
    boolean skip() default false;
}
