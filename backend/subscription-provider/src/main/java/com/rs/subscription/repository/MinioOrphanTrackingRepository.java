package com.rs.subscription.repository;

import com.rs.subscription.entity.MinioOrphanTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MinioOrphanTrackingRepository extends JpaRepository<MinioOrphanTracking, Long> {

    List<MinioOrphanTracking> findByConfirmedFalseAndUploadedAtBefore(LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE MinioOrphanTracking t SET t.confirmed = true " +
           "WHERE t.bucket = :bucket AND t.objectName = :objectName")
    void confirmByBucketAndObjectName(@Param("bucket") String bucket,
                                      @Param("objectName") String objectName);
}
