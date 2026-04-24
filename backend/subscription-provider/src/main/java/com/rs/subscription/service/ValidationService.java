package com.rs.subscription.service;

import com.rs.subscription.dto.response.ValidationResponse;
import com.rs.subscription.entity.GroupMember;
import com.rs.subscription.entity.Plan;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupMemberRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final SubscriptionRepository subscriptionRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ValidationResponse validateSubscription(String userId, Long groupId) {
        if (userId != null) {
            return validateByUserId(userId);
        } else if (groupId != null) {
            Subscription sub = subscriptionRepository
                .findByGroupIdAndStatus(groupId, Subscription.SubscriptionStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new SmsException(ErrorCodes.NO_ACTIVE_GROUP_SUBSCRIPTION,
                    "No active subscription for group: " + groupId, 403));
            return buildResponse(sub, sub.getEndDate(), userId);
        } else {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "userId or groupId is required", 400);
        }
    }

    private ValidationResponse validateByUserId(String userId) {
        // 1. Check individual subscription first — pick the one with most remaining quota
        var individualSubs = subscriptionRepository
            .findByUserIdAndStatusInOrderByCreatedAtDesc(userId,
                List.of(Subscription.SubscriptionStatus.ACTIVE));
        if (!individualSubs.isEmpty()) {
            Subscription sub = individualSubs.stream()
                .filter(s -> s.getEndDate() == null || !s.getEndDate().isBefore(LocalDate.now()))
                .max(java.util.Comparator.comparingInt(
                    s -> s.getSigningQuotaTotal() - s.getSigningQuotaUsed()))
                .orElse(individualSubs.get(0));
            return buildResponse(sub, sub.getEndDate(), userId);
        }

        // 2. Fall back to group membership
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        for (GroupMember membership : memberships) {
            Long gid = membership.getGroup().getGroupId();
            List<Subscription> activeGroupSubs = subscriptionRepository
                .findByGroupIdAndStatus(gid, Subscription.SubscriptionStatus.ACTIVE);

            for (Subscription groupSub : activeGroupSubs) {
                Plan plan = groupSub.getPlan();
                if (plan.getGroupMemberValidityMode() == Plan.GroupMemberValidityMode.INDIVIDUAL_START) {
                    if (membership.getMemberEndDate() == null) continue;
                    if (LocalDate.now().isAfter(membership.getMemberEndDate())) continue;
                    return buildResponse(groupSub, membership.getMemberEndDate(), userId);
                } else {
                    // GROUP_FOLLOWS: use group subscription dates
                    return buildResponse(groupSub, groupSub.getEndDate(), userId);
                }
            }
        }

        throw new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
            "No active subscription for user: " + userId, 404);
    }

    private ValidationResponse buildResponse(Subscription sub, LocalDate effectiveEndDate, String userId) {
        ValidationResponse r = new ValidationResponse();
        r.setUserId(userId != null ? userId : sub.getUserId());
        r.setSubscriptionId(sub.getSubscriptionId());
        r.setStatus(sub.getStatus().name());
        r.setQuotaRemaining(sub.getSigningQuotaTotal() - sub.getSigningQuotaUsed());
        r.setEndDate(effectiveEndDate);
        ValidationResponse.FeatureFlagsDto ff = new ValidationResponse.FeatureFlagsDto();
        ff.setAllowBulkSigning(sub.getPlan().getAllowBulkSigning());
        ff.setAllowApiAccess(sub.getPlan().getAllowApiAccess());
        r.setFeatureFlags(ff);
        return r;
    }
}
