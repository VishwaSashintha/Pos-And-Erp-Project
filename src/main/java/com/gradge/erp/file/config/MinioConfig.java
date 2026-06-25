package com.gradge.erp.file.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("gradge", "gradge123")
                .build();
    }
}
