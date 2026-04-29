package com.rs.subscription.aop;

import com.rs.subscription.entity.AdminAuditLog;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.SubscriptionAuditLog;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.repository.AdminAuditLogRepository;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import com.rs.subscription.repository.SubscriptionAuditLogRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class DatabaseAuditAspect {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;
    private final GroupPlanAssignmentRepository groupPlanAssignmentRepository;
    private final RetailPlanScheduleRepository retailPlanScheduleRepository;
    private final AssignmentAuditRepository assignmentAuditRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserAccountRepository userAccountRepository;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(trackSubscriptionAudit)")
    public Object trackSubscriptionAudit(ProceedingJoinPoint joinPoint, TrackSubscriptionAudit trackSubscriptionAudit) throws Throwable {
        Long subscriptionIdBefore = toLong(evaluate(trackSubscriptionAudit.subscriptionId(), joinPoint, null));
        Subscription before = subscriptionIdBefore == null ? null : subscriptionRepository.findById(subscriptionIdBefore).orElse(null);

        Object result = joinPoint.proceed();

        Long subscriptionId = subscriptionIdBefore;
        if (subscriptionId == null) {
            subscriptionId = toLong(evaluate(trackSubscriptionAudit.subscriptionId(), joinPoint, result));
        }
        if (subscriptionId == null) {
            return result;
        }

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null) {
            return result;
        }

        String actor = stringValue(evaluate(trackSubscriptionAudit.actor(), joinPoint, result));
        String reason = stringValue(evaluate(trackSubscriptionAudit.reason(), joinPoint, result));
        String sourceType = stringValue(evaluate(trackSubscriptionAudit.sourceType(), joinPoint, result));
        if (trackSubscriptionAudit.resolveActorFromUserId() && actor != null && !actor.isBlank()) {
            actor = findUserId(actor).flatMap(userAccountRepository::findById).map(user -> user.getUsername()).orElse(actor);
        }
        if (sourceType == null || sourceType.isBlank()) {
            sourceType = subscription.getSourceType();
        }

        subscriptionAuditLogRepository.save(SubscriptionAuditLog.builder()
            .subscription(subscription)
            .actor(actor == null || actor.isBlank() ? "SYSTEM" : actor)
            .oldStatus(before != null ? before.getStatus() : null)
            .newStatus(subscription.getStatus())
            .reason(reason)
            .sourceType(sourceType)
            .build());
        return result;
    }

    @Around("@annotation(trackAssignmentAudit)")
    public Object trackAssignmentAudit(ProceedingJoinPoint joinPoint, TrackAssignmentAudit trackAssignmentAudit) throws Throwable {
        String assignmentType = trackAssignmentAudit.assignmentType().toUpperCase();
        Long entityIdBefore = toLong(evaluate(trackAssignmentAudit.entityId(), joinPoint, null));
        String oldStatus = resolveAssignmentStatus(assignmentType, entityIdBefore);

        Object result = joinPoint.proceed();

        Long entityId = entityIdBefore;
        if (entityId == null) {
            entityId = toLong(evaluate(trackAssignmentAudit.entityId(), joinPoint, result));
        }
        if (entityId == null) {
            return result;
        }

        String actor = stringValue(evaluate(trackAssignmentAudit.actor(), joinPoint, result));
        String note = stringValue(evaluate(trackAssignmentAudit.note(), joinPoint, result));
        String actionValue = stringValue(evaluate(trackAssignmentAudit.action(), joinPoint, result));
        String action = CommercialEnums.normalize(actionValue, CommercialEnums.AuditAction.class, "action");

        if (CommercialEnums.AssignmentType.GROUP_PLAN.name().equals(assignmentType)) {
            groupPlanAssignmentRepository.findById(entityId).ifPresent(entity ->
                assignmentAuditRepository.save(AssignmentAudit.builder()
                    .groupPlanAssignment(entity)
                    .assignmentType(CommercialEnums.AssignmentType.GROUP_PLAN.name())
                    .action(action)
                    .oldStatus(oldStatus)
                    .newStatus(entity.getAssignmentStatus())
                    .actor(actor)
                    .note(note)
                    .build())
            );
        } else if (CommercialEnums.AssignmentType.RETAIL_PLAN.name().equals(assignmentType)) {
            retailPlanScheduleRepository.findById(entityId).ifPresent(entity ->
                assignmentAuditRepository.save(AssignmentAudit.builder()
                    .retailPlanSchedule(entity)
                    .assignmentType(CommercialEnums.AssignmentType.RETAIL_PLAN.name())
                    .action(action)
                    .oldStatus(oldStatus)
                    .newStatus(entity.getScheduleStatus())
                    .actor(actor)
                    .note(note)
                    .build())
            );
        }
        return result;
    }

    @Around("@annotation(trackAdminAudit)")
    public Object trackAdminAudit(ProceedingJoinPoint joinPoint, TrackAdminAudit trackAdminAudit) throws Throwable {
        Object result = joinPoint.proceed();

        String actor = stringValue(evaluate(trackAdminAudit.actor(), joinPoint, result));
        if (trackAdminAudit.resolveActorFromUserId()) {
            actor = findUserId(actor).flatMap(userAccountRepository::findById).map(user -> user.getUsername()).orElse(actor);
        }

        adminAuditLogRepository.save(AdminAuditLog.builder()
            .actor(actor)
            .action(stringValue(evaluate(trackAdminAudit.action(), joinPoint, result)))
            .entityType(stringValue(evaluate(trackAdminAudit.entityType(), joinPoint, result)))
            .entityId(stringValue(evaluate(trackAdminAudit.entityId(), joinPoint, result)))
            .details(stringValue(evaluate(trackAdminAudit.details(), joinPoint, result)))
            .build());
        return result;
    }

    private String resolveAssignmentStatus(String assignmentType, Long entityId) {
        if (entityId == null) {
            return null;
        }
        if (CommercialEnums.AssignmentType.GROUP_PLAN.name().equals(assignmentType)) {
            return groupPlanAssignmentRepository.findById(entityId)
                .map(GroupPlanAssignment::getAssignmentStatus)
                .orElse(null);
        }
        if (CommercialEnums.AssignmentType.RETAIL_PLAN.name().equals(assignmentType)) {
            return retailPlanScheduleRepository.findById(entityId)
                .map(RetailPlanSchedule::getScheduleStatus)
                .orElse(null);
        }
        return null;
    }

    private Optional<Long> findUserId(String value) {
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Object evaluate(String expression, ProceedingJoinPoint joinPoint, Object result) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        if (!expression.contains("#") && !expression.contains("'")) {
            return expression;
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method)).orElse(new String[0]);
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (i < parameterNames.length) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        context.setVariable("result", result);
        Expression spel = parser.parseExpression(expression);
        try {
            return spel.getValue(context);
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}


