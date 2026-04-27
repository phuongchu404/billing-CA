package com.rs.subscription.aop;

import com.rs.subscription.entity.AdminAuditLog;
import com.rs.subscription.repository.AdminAuditLogRepository;
import com.rs.subscription.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAuditAspect {

    private static final Set<String> READ_PREFIXES = Set.of(
        "get", "find", "list", "search", "exists", "has", "is", "count",
        "load", "fetch", "page", "query", "check", "resolve", "to", "build"
    );

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("(@within(com.rs.subscription.aop.Auditable) || @annotation(com.rs.subscription.aop.Auditable))" +
            " && execution(public * *(..))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();

        Auditable methodAnn = method.getAnnotation(Auditable.class);
        Auditable classAnn  = joinPoint.getTarget().getClass().getAnnotation(Auditable.class);

        // Method-level skip always wins
        if (methodAnn != null && methodAnn.skip()) {
            return joinPoint.proceed();
        }

        // Class-level: skip read-like methods unless there is an explicit method annotation
        if (methodAnn == null && isReadMethod(method.getName())) {
            return joinPoint.proceed();
        }

        Auditable ann = methodAnn != null ? methodAnn : classAnn;

        Object result = joinPoint.proceed();

        try {
            persist(ann, joinPoint, method, result);
        } catch (Exception ex) {
            log.warn("AdminAuditAspect: failed to persist audit log for {}", method.getName(), ex);
        }

        return result;
    }

    // -------------------------------------------------------------------------

    private void persist(Auditable ann, ProceedingJoinPoint joinPoint, Method method, Object result) {
        String action     = resolveAction(ann, method.getName());
        String entityType = resolveEntityType(ann, joinPoint.getTarget().getClass().getSimpleName());
        String entityId   = resolveEntityId(ann, joinPoint, method, result);
        String details    = spel(ann.details(), joinPoint, method, result);
        String actor      = resolveActor();

        adminAuditLogRepository.save(AdminAuditLog.builder()
            .actor(actor)
            .action(action)
            .entityType(entityType)
            .entityId(entityId != null ? entityId : "")
            .details(details)
            .build());
    }

    /** Derive action: annotation value wins, otherwise camelCase → SCREAMING_SNAKE. */
    private String resolveAction(Auditable ann, String methodName) {
        if (!ann.action().isBlank()) return ann.action();
        return camelToScreaming(methodName);
    }

    /** Derive entityType: annotation value wins, otherwise XyzService → XYZ. */
    private String resolveEntityType(Auditable ann, String simpleClassName) {
        if (!ann.entityType().isBlank()) return ann.entityType();
        String base = simpleClassName.replace("ServiceImpl", "").replace("Service", "");
        return camelToScreaming(base);
    }

    /**
     * Resolve entityId:
     * 1. SpEL expression on annotation
     * 2. Common getter on return value (getId, getUserId, …)
     * 3. First String/Long parameter
     */
    private String resolveEntityId(Auditable ann, ProceedingJoinPoint joinPoint, Method method, Object result) {
        if (!ann.entityId().isBlank()) {
            return spel(ann.entityId(), joinPoint, method, result);
        }
        String fromResult = extractIdFromResult(result);
        if (fromResult != null) return fromResult;
        return extractIdFromArgs(joinPoint.getArgs(), method.getParameterTypes());
    }

    private String extractIdFromResult(Object result) {
        if (result == null) return null;
        if (result instanceof String s) return s;
        if (result instanceof Long || result instanceof Integer) return String.valueOf(result);

        // Try getters that look like IDs, prefer shorter names first (getId before getUserId)
        String found = null;
        int bestLen = Integer.MAX_VALUE;
        for (Method m : result.getClass().getMethods()) {
            String name = m.getName();
            if (m.getParameterCount() != 0) continue;
            if (!name.startsWith("get") || !name.endsWith("Id")) continue;
            if (m.getReturnType() != String.class && !Number.class.isAssignableFrom(m.getReturnType())) continue;
            try {
                Object val = m.invoke(result);
                if (val != null && name.length() < bestLen) {
                    found = String.valueOf(val);
                    bestLen = name.length();
                }
            } catch (Exception ignored) {}
        }
        return found;
    }

    private String extractIdFromArgs(Object[] args, Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            Class<?> t = types[i];
            if ((t == String.class || t == Long.class || t == long.class) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }

    /** Get the current authenticated username, or "SYSTEM" if unauthenticated. */
    private String resolveActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails u) {
                return u.getUsername();
            }
        } catch (Exception ignored) {}
        return "SYSTEM";
    }

    /** Evaluate a SpEL expression with method args and result in scope. Returns null on failure. */
    private String spel(String expression, ProceedingJoinPoint joinPoint, Method method, Object result) {
        if (expression == null || expression.isBlank()) return null;
        try {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            String[] names = Optional.ofNullable(paramNameDiscoverer.getParameterNames(method)).orElse(new String[0]);
            for (int i = 0; i < args.length; i++) {
                ctx.setVariable("p" + i, args[i]);
                ctx.setVariable("a" + i, args[i]);
                if (i < names.length) ctx.setVariable(names[i], args[i]);
            }
            ctx.setVariable("result", result);
            Expression expr = spelParser.parseExpression(expression);
            Object val = expr.getValue(ctx);
            return val == null ? null : String.valueOf(val);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isReadMethod(String name) {
        for (String prefix : READ_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    /** createUser → CREATE_USER, UserService → USER_SERVICE */
    private String camelToScreaming(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}
