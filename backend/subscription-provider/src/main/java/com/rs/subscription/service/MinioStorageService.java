package com.rs.subscription.service;

import com.rs.subscription.dto.response.FileUploadResponse;
import com.rs.subscription.entity.MinioOrphanTracking;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.repository.MinioOrphanTrackingRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final MinioClient minioClient;
    private final MinioOrphanTrackingRepository orphanRepo;

    @Value("${minio.bucket-public}")
    private String publicBucket;

    @Value("${minio.public-url}")
    private String publicUrl;

    /**
     * Uploads an image to MinIO public bucket under the given area prefix.
     * Tracks as orphan until {@link #confirmByStoragePath} is called.
     */
    public FileUploadResponse uploadImage(MultipartFile file, String area) {
        validateImage(file);

        String objectName = buildObjectName(file.getOriginalFilename(), area);
        String contentType = file.getContentType();

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(publicBucket)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception ex) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Không thể upload ảnh: " + ex.getMessage(), 500);
        }

        orphanRepo.save(MinioOrphanTracking.builder()
                .bucket(publicBucket)
                .objectName(objectName)
                .build());

        String storagePath = publicBucket + "/" + objectName;
        return FileUploadResponse.builder()
                .bucket(publicBucket)
                .objectName(objectName)
                .storagePath(storagePath)
                .url(toPublicUrl(storagePath))
                .contentType(contentType)
                .size(file.getSize())
                .build();
    }

    /** Marks an uploaded file as used — prevents it from being deleted by the cleanup job. */
    public void confirmByStoragePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return;
        int slash = storagePath.indexOf('/');
        if (slash < 1) return;
        orphanRepo.confirmByBucketAndObjectName(
                storagePath.substring(0, slash),
                storagePath.substring(slash + 1));
    }

    /** Deletes a file from MinIO by its storagePath. */
    public void deleteByStoragePath(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return;
        int slash = storagePath.indexOf('/');
        if (slash < 1) return;
        String bucket = storagePath.substring(0, slash);
        String objectName = storagePath.substring(slash + 1);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to delete MinIO object [{}/{}]: {}", bucket, objectName, ex.getMessage());
        }
    }

    /**
     * Converts a storagePath or full URL to a full public URL for browser display.
     * storagePath format: "{bucket}/{objectName}"
     */
    public String toPublicUrl(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return null;
        if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) return storagePath;
        return publicUrl.replaceAll("/+$", "") + "/" + storagePath;
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "File ảnh không được để trống", 400);
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Chỉ chấp nhận ảnh JPEG, PNG, WEBP hoặc GIF", 400);
        }
    }

    private String buildObjectName(String originalFilename, String area) {
        LocalDate today = LocalDate.now();
        String normalizedArea = normalizeSegment(area);
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String filename = UUID.randomUUID() + (ext == null ? "" : "." + ext.toLowerCase(Locale.ROOT));
        return String.join("/",
                "images", normalizedArea,
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth()),
                filename);
    }

    private String normalizeSegment(String value) {
        if (value == null || value.isBlank()) return "general";
        String s = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-").replaceAll("^-+|-+$", "");
        return s.isBlank() ? "general" : s;
    }
}
