package com.rs.subscription.repository;

import com.rs.subscription.entity.DocumentUploadRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentUploadRecordRepository extends JpaRepository<DocumentUploadRecord, Long> {

    @Query(nativeQuery = true,
           value = "SELECT WEEK(d.uploaded_at, 1) - WEEK(DATE_FORMAT(d.uploaded_at, '%Y-%m-01'), 1) + 1 AS week_num, " +
                   "COUNT(d.id) " +
                   "FROM document_upload_records d " +
                   "JOIN subscriptions s ON s.subscription_id = d.subscription_id " +
                   "WHERE s.subscriber_type = 'INDIVIDUAL' " +
                   "AND d.upload_status = 'SUCCESS' " +
                   "AND d.uploaded_at >= :from AND d.uploaded_at < :to " +
                   "GROUP BY week_num " +
                   "ORDER BY week_num")
    List<Object[]> countWeeklySuccessfulUploadsForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(d) FROM DocumentUploadRecord d " +
           "WHERE d.subscription.subscriberType = 'INDIVIDUAL' " +
           "AND d.uploadStatus = 'SUCCESS' " +
           "AND d.uploadedAt >= :from AND d.uploadedAt < :to")
    long countSuccessfulUploadsForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(d) FROM DocumentUploadRecord d " +
           "WHERE d.subscription.subscriberType = 'INDIVIDUAL' " +
           "AND d.uploadedAt >= :from AND d.uploadedAt < :to")
    long countUploadsForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}
