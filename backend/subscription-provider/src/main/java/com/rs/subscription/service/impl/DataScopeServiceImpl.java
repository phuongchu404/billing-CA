package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PartnerGroupAccessRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resolves the set of Group IDs that the currently-authenticated user is
 * allowed to see, based on their permission set:
 *
 *  ADMIN / no scope-permission  → all groups (null = no filter)
 *  group:view:own               → groups owned by current user
 *  group:view:subordinates      → groups owned by current user + all subordinates
 *  report:view:partner          → groups explicitly granted via partner_group_access
 *
 * Returns null to mean "no filter — see everything".
 * Returns an empty list when the user should see nothing.
 */
@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {

    private final UserAccountRepository userAccountRepository;
    private final GroupRepository groupRepository;
    private final PartnerGroupAccessRepository partnerGroupAccessRepository;

    /**
     * Returns the list of groupIds visible to the current user,
     * or null if all groups should be visible.
     */
    @Transactional(readOnly = true)
    public List<Long> resolveVisibleGroupIds() {
        CustomUserDetails user = currentUser();
        if (user == null) return List.of();

        boolean isPartner       = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTNER"));
        boolean viewOwn         = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("group:view:own"));
        boolean viewSubordinates= user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("group:view:subordinates"));
        boolean isAdmin         = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Admin / full-access roles: no filter
        if (isAdmin) {
            return null;
        }

        if (!isPartner && !viewOwn && !viewSubordinates) {
            return null;
        }

        if (isPartner) {
            return partnerGroupAccessRepository.findActiveGroupIdsByPartner(user.getUserId());
        }

        // viewOwn or viewSubordinates
        List<Long> ownerIds = new ArrayList<>();
        ownerIds.add(user.getUserId());

        if (viewSubordinates) {
            List<Long> subordinates = userAccountRepository.findAllSubordinateIds(user.getUserId());
            ownerIds.addAll(subordinates);
        }

        return groupRepository.findByOwnerUserIdInOrderByGroupId(ownerIds)
                .stream()
                .map(g -> g.getGroupId())
                .toList();
    }

    /**
     * Returns all ownerUserIds the current user can manage (self + subordinates).
     * Returns null if the user can see all.
     */
    @Transactional(readOnly = true)
    public List<Long> resolveVisibleOwnerIds() {
        CustomUserDetails user = currentUser();
        if (user == null) return List.of();

        boolean viewOwn          = hasAuthority(user, "group:view:own");
        boolean viewSubordinates = hasAuthority(user, "group:view:subordinates");
        boolean isPartner        = hasAuthority(user, "ROLE_PARTNER");
        boolean isAdmin          = hasAuthority(user, "ROLE_ADMIN");

        if (isAdmin) return null;
        if (!viewOwn && !viewSubordinates && !isPartner) return null;
        if (isPartner) return List.of(); // partners don't own groups

        List<Long> ids = new ArrayList<>();
        ids.add(user.getUserId());
        if (viewSubordinates) {
            ids.addAll(userAccountRepository.findAllSubordinateIds(user.getUserId()));
        }
        return ids;
    }

    /** Returns current user's userId. */
    public Long currentUserId() {
        CustomUserDetails u = currentUser();
        return u != null ? u.getUserId() : null;
    }

    /**
     * Checks whether a given groupId is accessible to the current user.
     * Used in getById/update/delete to prevent unauthorized access.
     */
    @Transactional(readOnly = true)
    public boolean canAccessGroup(Long groupId) {
        List<Long> visible = resolveVisibleGroupIds();
        if (visible == null) return true; // no filter = admin
        return visible.contains(groupId);
    }

    private CustomUserDetails currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;
        return (CustomUserDetails) auth.getPrincipal();
    }

    private boolean hasAuthority(CustomUserDetails user, String authority) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }
}


