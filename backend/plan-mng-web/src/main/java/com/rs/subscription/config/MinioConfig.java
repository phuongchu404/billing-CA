package com.rs.subscription.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-public}")
    private String publicBucket;

    @Value("${minio.public-policy}")
    private String publicPolicyResource;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        initializeBucket(client, publicBucket, true);
        return client;
    }

    private void initializeBucket(MinioClient client, String bucket, boolean applyPublicPolicy) {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO: created bucket '{}'", bucket);
            }
            if (applyPublicPolicy) {
                String policy = StreamUtils.copyToString(
                        new ClassPathResource(publicPolicyResource).getInputStream(),
                        StandardCharsets.UTF_8)
                        .replace("${bucketName}", bucket);
                client.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(policy)
                        .build());
            }
        } catch (Exception ex) {
            log.error("MinIO: unable to initialize bucket '{}': {}", bucket, ex.getMessage());
        }
    }
}
