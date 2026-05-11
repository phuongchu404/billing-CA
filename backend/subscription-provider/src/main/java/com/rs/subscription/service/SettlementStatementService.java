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

public interface SettlementStatementService {

    List<SettlementStatementResponse> listAll();

    List<SettlementStatementResponse> listByGroup(Long groupId);

    SettlementStatementResponse create(CreateSettlementStatementRequest request);

    /**
     * Xuất Excel đối soát.
     * @param groupId null = tất cả đại lý
     * @param month   "YYYY-MM" (null = không lọc tháng)
     */
    byte[] exportToExcel(Long groupId, String month);
}
