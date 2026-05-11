package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementStatementServiceImpl implements SettlementStatementService {

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

    @Override
    public byte[] exportToExcel(Long groupId, String month) {
        LocalDate start = null;
        LocalDate end = null;
        if (month != null && !month.isBlank()) {
            YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        }

        List<SettlementStatement> rows = settlementStatementRepository.findForExport(groupId, start, end);

        // SXSSFWorkbook: flush 500 rows vào disk, không giữ toàn bộ trong RAM
        try (SXSSFWorkbook wb = new SXSSFWorkbook(500)) {
            Sheet sheet = wb.createSheet("Doi Soat");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            String[] headers = groupId == null
                ? new String[]{"#", "Mã đại lý", "Tên đại lý", "Kỳ từ", "Kỳ đến", "Trạng thái",
                               "Tổng chứng thư", "Tổng ký số", "Tổng tiền", "Tiền tệ", "Ngày tạo", "Người tạo"}
                : new String[]{"#", "Kỳ từ", "Kỳ đến", "Trạng thái",
                               "Tổng chứng thư", "Tổng ký số", "Tổng tiền", "Tiền tệ", "Ngày tạo", "Người tạo"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (int r = 0; r < rows.size(); r++) {
                SettlementStatement s = rows.get(r);
                Row row = sheet.createRow(r + 1);
                int col = 0;
                row.createCell(col++).setCellValue(r + 1);
                if (groupId == null) {
                    row.createCell(col++).setCellValue(s.getGroup().getGroupCode());
                    row.createCell(col++).setCellValue(s.getGroup().getGroupName());
                }
                row.createCell(col++).setCellValue(s.getFromDate() != null ? s.getFromDate().format(dateFmt) : "");
                row.createCell(col++).setCellValue(s.getToDate()   != null ? s.getToDate().format(dateFmt)   : "");
                row.createCell(col++).setCellValue(s.getStatus());

                Cell ctsCell = row.createCell(col++);
                ctsCell.setCellValue(s.getTotalCertificates() != null ? s.getTotalCertificates() : 0);

                Cell sigCell = row.createCell(col++);
                sigCell.setCellValue(s.getTotalSignings() != null ? s.getTotalSignings() : 0);

                Cell amtCell = row.createCell(col++);
                amtCell.setCellValue(s.getTotalAmount() != null ? s.getTotalAmount().doubleValue() : 0);

                row.createCell(col++).setCellValue(s.getCurrency());
                row.createCell(col++).setCellValue(s.getGeneratedAt() != null ? s.getGeneratedAt().format(dtFmt) : "");
                row.createCell(col++).setCellValue(s.getGeneratedBy() != null ? s.getGeneratedBy() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose(); // xoá temp files của SXSSFWorkbook
            return out.toByteArray();
        } catch (IOException e) {
            throw new SmsException(ErrorCodes.INTERNAL_ERROR, "Không thể xuất file Excel: " + e.getMessage(), 500);
        }
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
