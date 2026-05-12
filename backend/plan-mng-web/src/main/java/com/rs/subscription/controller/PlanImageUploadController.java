package com.rs.subscription.controller;

import com.rs.subscription.dto.response.FileUploadResponse;
import com.rs.subscription.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class PlanImageUploadController {

    private final MinioStorageService minioStorageService;

    /**
     * Upload icon image for a plan subject card.
     * Returns storagePath and full public URL.
     * The storagePath should be sent back when saving the plan to confirm the image.
     */
    @PostMapping(value = "/plan-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('plan:create') or hasAuthority('plan:update')")
    public ResponseEntity<FileUploadResponse> uploadPlanIcon(
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = minioStorageService.uploadImage(file, "plans");
        return ResponseEntity.ok(response);
    }
}
