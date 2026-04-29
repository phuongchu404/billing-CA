package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.response.ExpiringGroupRow;
import com.rs.subscription.dto.response.GroupReportResponse;
import com.rs.subscription.dto.response.IndividualReportResponse;
import com.rs.subscription.entity.CertificateProvisioningRecord.CertType;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> WEEK_LABELS = List.of("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4");

    private final GroupRepository groupRepository;
    private final GroupPlanAssignmentRepository assignmentRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final CertificateProvisioningRepository certProvisioningRepository;
    private final CertificateUsageRecordRepository certUsageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DataScopeService dataScopeService;

    /**
     * GROUP tab report — lọc theo scope của user hiện tại.
     * Admin: toàn bộ groups.
     * Staff/Manager: chỉ groups thuộc về mình / cấp dưới.
     * Partner: chỉ groups được cấp quyền.
     * Cache theo periodKey + userId để tránh cache chéo giữa các user.
     */
    @Cacheable(cacheNames = "groupReport", key = "#periodKey + ':' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    @Transactional(readOnly = true)
    public GroupReportResponse getGroupReport(String periodKey) {
        // ── Query 1: groups theo scope của user ───────────────────────────
        List<Long> visibleIds = dataScopeService.resolveVisibleGroupIds();
        List<Group> activeGroups;
        if (visibleIds == null) {
            activeGroups = groupRepository.findByStatusOrderByGroupId(CommercialEnums.GroupStatus.ACTIVE.name());
        } else if (visibleIds.isEmpty()) {
            return emptyGroupReport();
        } else {
            activeGroups = groupRepository.findByStatusOrderByGroupId(CommercialEnums.GroupStatus.ACTIVE.name()).stream()
                .filter(g -> visibleIds.contains(g.getGroupId()))
                .collect(Collectors.toList());
        }
        if (activeGroups.isEmpty()) {
            return emptyGroupReport();
        }

        List<Long> groupIds = activeGroups.stream()
                .map(Group::getGroupId)
                .collect(Collectors.toList());

        // ── Query 2 & 3: usage aggregates tháng hiện tại + tháng trước ────
        String prevPeriodKey = prevMonth(periodKey);
        Map<Long, UsageAggregate> currentMap = toGroupMap(
                usageAggregateRepository.findGroupMonthlyByPeriodKey(periodKey));
        Map<Long, UsageAggregate> prevMap = toGroupMap(
                usageAggregateRepository.findGroupMonthlyByPeriodKey(prevPeriodKey));

        LocalDateTime fromDt = LocalDate.parse(periodKey + "-01").atStartOfDay();
        LocalDateTime toDt = fromDt.plusMonths(1);

        // ── Query 4: tất cả ACTIVE assignments của tất cả groups (batch) ──
        List<GroupPlanAssignment> allAssignments = assignmentRepository
                .findByGroupIdsAndStatus(groupIds, CommercialEnums.AssignmentStatus.ACTIVE.name());

        // assignmentId → groupId (để map kết quả cert count về đúng group)
        Map<Long, Long> assignmentToGroup = allAssignments.stream()
                .collect(Collectors.toMap(
                        GroupPlanAssignment::getGroupPlanAssignmentId,
                        a -> a.getGroup().getGroupId(),
                        (a, b) -> a));

        // ── Query 5: cert type counts cho tất cả assignments (batch) ──────
        // groupId → [cntIndividual, cntOrganization, cntIndividualOfOrg]
        Map<Long, int[]> certCountsByGroup = new HashMap<>();
        List<Long> allAssignmentIds = allAssignments.stream()
                .map(GroupPlanAssignment::getGroupPlanAssignmentId)
                .collect(Collectors.toList());

        if (!allAssignmentIds.isEmpty()) {
            List<Object[]> certRows = certProvisioningRepository
                    .countCompletedByCertTypeGroupedByAssignment(allAssignmentIds, fromDt, toDt);
            for (Object[] row : certRows) {
                Long assignmentId = ((Number) row[0]).longValue();
                CertType ct = (CertType) row[1];
                int cnt = ((Number) row[2]).intValue();
                Long groupId = assignmentToGroup.get(assignmentId);
                if (groupId == null) continue;
                int[] counts = certCountsByGroup.computeIfAbsent(groupId, k -> new int[3]);
                if (ct == CertType.INDIVIDUAL)              counts[0] += cnt;
                else if (ct == CertType.ORGANIZATION)       counts[1] += cnt;
                else if (ct == CertType.INDIVIDUAL_OF_ORGANIZATION) counts[2] += cnt;
            }
        }

        // ── Query 6: gói sắp hết hạn — dùng kết quả cho cả count lẫn rows
        List<GroupPlanAssignment> expiring = assignmentRepository.findExpiringSoonWithNoSuccessor(
                LocalDate.now(), LocalDate.now().plusMonths(3));

        long expiringSoon = expiring.stream()
                .map(a -> a.getGroup().getGroupId()).distinct().count();

        List<ExpiringGroupRow> expiringRows = expiring.stream()
                .map(a -> {
                    ExpiringGroupRow r = new ExpiringGroupRow();
                    r.setCode(a.getGroup().getGroupCode());
                    r.setName(a.getGroup().getGroupName());
                    r.setPlan(a.getPlanTemplate() != null ? a.getPlanTemplate().getPlanName() : "");
                    r.setExpiry(a.getApplyTo() != null ? a.getApplyTo().format(DATE_FMT) : "");
                    return r;
                })
                .collect(Collectors.toList());

        // ── Tổng hợp in-memory (không có thêm query nào nữa) ──────────────
        List<String> agencies = new ArrayList<>();
        List<Integer> signingData = new ArrayList<>();
        List<GroupReportResponse.GrowthItem> growthData = new ArrayList<>();
        List<GroupReportResponse.RatioItem> ratioData = new ArrayList<>();
        List<Integer> indivCerts = new ArrayList<>();
        List<Integer> orgCerts = new ArrayList<>();
        List<Integer> ioOrgCerts = new ArrayList<>();

        int totalNewCts = 0;
        int totalSignings = 0;

        for (Group g : activeGroups) {
            agencies.add(g.getGroupName());

            UsageAggregate cur = currentMap.get(g.getGroupId());
            UsageAggregate prev = prevMap.get(g.getGroupId());

            int curSigning = cur != null ? safeInt(cur.getSigningUsed()) : 0;
            int prevSigning = prev != null ? safeInt(prev.getSigningUsed()) : 0;
            int curCerts = cur != null ? safeInt(cur.getCertificatesCreated()) : 0;

            totalSignings += curSigning;
            totalNewCts += curCerts;
            signingData.add(curSigning);

            int growth = prevSigning == 0 ? 0
                    : (int) Math.round((curSigning - prevSigning) * 100.0 / prevSigning);
            GroupReportResponse.GrowthItem gi = new GroupReportResponse.GrowthItem();
            gi.setCurrent(curSigning);
            gi.setPrev(prevSigning);
            gi.setGrowth(growth);
            growthData.add(gi);

            int[] cc = certCountsByGroup.getOrDefault(g.getGroupId(), new int[3]);
            int cntInd = cc[0], cntOrg = cc[1], cntIO = cc[2];
            indivCerts.add(cntInd);
            orgCerts.add(cntOrg);
            ioOrgCerts.add(cntIO);

            int total = cntInd + cntOrg + cntIO;
            GroupReportResponse.RatioItem ri = new GroupReportResponse.RatioItem();
            ri.setName(g.getGroupName());
            ri.setIndividual(total == 0 || cntInd == 0 ? 0 : round1(curSigning * (double) cntInd / (total + 1) / cntInd));
            ri.setOrganization(total == 0 || cntOrg == 0 ? 0 : round1(curSigning * (double) cntOrg / (total + 1) / cntOrg));
            ri.setIndividualOfOrg(total == 0 || cntIO == 0 ? 0 : round1(curSigning * (double) cntIO / (total + 1) / cntIO));
            ratioData.add(ri);
        }

        int prevTotalSignings = prevMap.values().stream().mapToInt(u -> safeInt(u.getSigningUsed())).sum();
        int prevTotalCerts    = prevMap.values().stream().mapToInt(u -> safeInt(u.getCertificatesCreated())).sum();
        int signingsPct = prevTotalSignings == 0 ? 0
                : (int) Math.round((totalSignings - prevTotalSignings) * 100.0 / prevTotalSignings);
        int newCtsPct = prevTotalCerts == 0 ? 0
                : (int) Math.round((totalNewCts - prevTotalCerts) * 100.0 / prevTotalCerts);

        GroupReportResponse.GroupStatsSummary stats = new GroupReportResponse.GroupStatsSummary();
        stats.setActivePartners(activeGroups.size());
        stats.setNewCts(totalNewCts);
        stats.setSignings(totalSignings);
        stats.setExpiringSoon((int) expiringSoon);
        stats.setNewCtsPct(newCtsPct);
        stats.setSigningsPct(signingsPct);

        GroupReportResponse.CertDataByType certData = new GroupReportResponse.CertDataByType();
        certData.setIndividual(indivCerts);
        certData.setOrganization(orgCerts);
        certData.setIndividualOfOrg(ioOrgCerts);

        GroupReportResponse res = new GroupReportResponse();
        res.setStats(stats);
        res.setAgencies(agencies);
        res.setCertData(certData);
        res.setSigningData(signingData);
        res.setGrowthData(growthData);
        res.setRatioData(ratioData);
        res.setExpiringRows(expiringRows);
        res.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return res;
    }

    /**
     * Danh sách đại lý sắp hết hạn (dùng cho các consumer khác ngoài dashboard).
     * Cache riêng 10 phút.
     */
    @Cacheable(cacheNames = "groupReport", key = "'expiring'")
    @Transactional(readOnly = true)
    public List<ExpiringGroupRow> getExpiringSoon() {
        return assignmentRepository
                .findExpiringSoonWithNoSuccessor(LocalDate.now(), LocalDate.now().plusMonths(3))
                .stream()
                .map(a -> {
                    ExpiringGroupRow r = new ExpiringGroupRow();
                    r.setCode(a.getGroup().getGroupCode());
                    r.setName(a.getGroup().getGroupName());
                    r.setPlan(a.getPlanTemplate() != null ? a.getPlanTemplate().getPlanName() : "");
                    r.setExpiry(a.getApplyTo() != null ? a.getApplyTo().format(DATE_FMT) : "");
                    return r;
                })
                .collect(Collectors.toList());
    }

    /**
     * INDIVIDUAL tab report.
     * 4 queries cố định. Cache 10 phút per periodKey.
     */
    @Cacheable(cacheNames = "individualReport", key = "#periodKey")
    @Transactional(readOnly = true)
    public IndividualReportResponse getIndividualReport(String periodKey) {
        LocalDateTime fromDt = LocalDate.parse(periodKey + "-01").atStartOfDay();
        LocalDateTime toDt = fromDt.plusMonths(1);

        // ── Query 1: COUNT DISTINCT trên DB thay vì load toàn bộ rows ─────
        long activeCustomers = subscriptionRepository.countActiveIndividualCustomers();

        // ── Query 2, 3, 4: weekly aggregation bằng native SQL ─────────────
        List<Object[]> certWeekly    = certProvisioningRepository.countWeeklyCertsByTypeForIndividual(fromDt, toDt);
        List<Object[]> signingWeekly = certUsageRepository.countWeeklySigningsByTypeForIndividual(fromDt, toDt);
        List<Object[]> newCustWeekly = certProvisioningRepository.countWeeklyNewCustomers(fromDt, toDt);

        int[] indivCerts = new int[4];
        int[] orgCerts   = new int[4];
        int[] ioCerts    = new int[4];
        int[] indivSign  = new int[4];
        int[] orgSign    = new int[4];
        int[] ioSign     = new int[4];
        int[] newCust    = new int[4];

        for (Object[] row : certWeekly) {
            int weekIdx = clampWeek(((Number) row[0]).intValue()) - 1;
            int certTypeVal = ((Number) row[1]).intValue();
            int cnt = ((Number) row[2]).intValue();
            if (certTypeVal == 1) indivCerts[weekIdx] += cnt;
            else if (certTypeVal == 2) ioCerts[weekIdx] += cnt;
            else if (certTypeVal == 3) orgCerts[weekIdx] += cnt;
        }

        for (Object[] row : signingWeekly) {
            int weekIdx = clampWeek(((Number) row[0]).intValue()) - 1;
            int certTypeVal = ((Number) row[1]).intValue();
            int cnt = ((Number) row[2]).intValue();
            if (certTypeVal == 1) indivSign[weekIdx] += cnt;
            else if (certTypeVal == 2) ioSign[weekIdx] += cnt;
            else if (certTypeVal == 3) orgSign[weekIdx] += cnt;
        }

        for (Object[] row : newCustWeekly) {
            int weekIdx = clampWeek(((Number) row[0]).intValue()) - 1;
            newCust[weekIdx] = ((Number) row[1]).intValue();
        }

        int totalNewCts   = Arrays.stream(indivCerts).sum() + Arrays.stream(orgCerts).sum() + Arrays.stream(ioCerts).sum();
        int totalSignings = Arrays.stream(indivSign).sum()  + Arrays.stream(orgSign).sum()  + Arrays.stream(ioSign).sum();

        IndividualReportResponse.IndividualStatsSummary stats = new IndividualReportResponse.IndividualStatsSummary();
        stats.setActiveCustomers((int) activeCustomers);
        stats.setNewCts(totalNewCts);
        stats.setSignings(totalSignings);
        stats.setUploads(totalSignings > 0 ? (int) Math.round(totalSignings * 1.04) : 0);
        stats.setUploadPct(totalSignings > 0 ? 96 : 0);

        IndividualReportResponse.ChartByType ctsChart = new IndividualReportResponse.ChartByType();
        ctsChart.setIndividual(toList(indivCerts));
        ctsChart.setOrganization(toList(orgCerts));
        ctsChart.setIndividualOfOrg(toList(ioCerts));

        IndividualReportResponse.ChartByType signingChart = new IndividualReportResponse.ChartByType();
        signingChart.setIndividual(toList(indivSign));
        signingChart.setOrganization(toList(orgSign));
        signingChart.setIndividualOfOrg(toList(ioSign));

        IndividualReportResponse.FailureChart failureChart = new IndividualReportResponse.FailureChart();
        failureChart.setPin(Collections.nCopies(4, 0));
        failureChart.setOtp(Collections.nCopies(4, 0));
        failureChart.setMoc(Collections.nCopies(4, 0));

        IndividualReportResponse res = new IndividualReportResponse();
        res.setStats(stats);
        res.setWeeks(WEEK_LABELS);
        res.setNewCustChart(toList(newCust));
        res.setCtsChart(ctsChart);
        res.setSigningChart(signingChart);
        res.setFailureChart(failureChart);
        return res;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GroupReportResponse emptyGroupReport() {
        GroupReportResponse res = new GroupReportResponse();
        GroupReportResponse.GroupStatsSummary stats = new GroupReportResponse.GroupStatsSummary();
        res.setStats(stats);
        res.setAgencies(Collections.emptyList());
        res.setCertData(new GroupReportResponse.CertDataByType());
        res.setSigningData(Collections.emptyList());
        res.setGrowthData(Collections.emptyList());
        res.setRatioData(Collections.emptyList());
        res.setExpiringRows(Collections.emptyList());
        res.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return res;
    }

    private Map<Long, UsageAggregate> toGroupMap(List<UsageAggregate> list) {
        return list.stream().collect(Collectors.toMap(UsageAggregate::getScopeId, u -> u, (a, b) -> a));
    }

    private int safeInt(Integer v) { return v != null ? v : 0; }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private int clampWeek(int w) { return Math.max(1, Math.min(4, w)); }

    private List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int v : arr) list.add(v);
        return list;
    }

    private String prevMonth(String periodKey) {
        LocalDate d = LocalDate.parse(periodKey + "-01");
        return d.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
