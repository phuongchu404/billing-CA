package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateSettlementStatementRequest;
import com.rs.subscription.dto.response.SettlementStatementResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.SettlementStatement;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.SettlementStatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementStatementService {

    private final SettlementStatementRepository settlementStatementRepository;
    private final GroupRepository groupRepository;

    public List<SettlementStatementResponse> listAll() {
        return settlementStatementRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<SettlementStatementResponse> listByGroup(Long groupId) {
        return settlementStatementRepository.findByGroupGroupIdOrderByCreatedAtDesc(groupId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public SettlementStatementResponse create(CreateSettlementStatementRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + request.getGroupId(), 404));
        SettlementStatement entity = SettlementStatement.builder()
            .group(group)
            .fromDate(request.getFromDate())
            .toDate(request.getToDate())
            .status(CommercialEnums.normalize(request.getStatus(), CommercialEnums.StatementStatus.class, "status"))
            .totalCertificates(request.getTotalCertificates())
            .totalSignings(request.getTotalSignings())
            .totalAmount(request.getTotalAmount())
            .currency(request.getCurrency())
            .generatedBy(request.getGeneratedBy())
            .generatedAt(LocalDateTime.now())
            .build();
        return toResponse(settlementStatementRepository.save(entity));
    }

    private SettlementStatementResponse toResponse(SettlementStatement entity) {
        SettlementStatementResponse response = new SettlementStatementResponse();
        response.setSettlementStatementId(entity.getSettlementStatementId());
        response.setGroupId(entity.getGroup().getGroupId());
        response.setGroupCode(entity.getGroup().getGroupCode());
        response.setGroupName(entity.getGroup().getGroupName());
        response.setFromDate(entity.getFromDate());
        response.setToDate(entity.getToDate());
        response.setStatus(entity.getStatus());
        response.setTotalCertificates(entity.getTotalCertificates());
        response.setTotalSignings(entity.getTotalSignings());
        response.setTotalAmount(entity.getTotalAmount());
        response.setCurrency(entity.getCurrency());
        response.setGeneratedAt(entity.getGeneratedAt());
        response.setGeneratedBy(entity.getGeneratedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
