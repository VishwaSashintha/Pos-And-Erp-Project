package com.gradge.erp.file.service;

import com.gradge.erp.file.entity.FileObject;
import com.gradge.erp.file.repository.FileRepository;
import com.gradge.erp.tenant.entity.Tenant;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileRepository fileRepository;
    private final MinioClient minioClient;

    public FileObject uploadFile(
            MultipartFile file,
            String module,
            UUID referenceId,
            Tenant tenant
    ) {
        try {
            String bucketName = "tenant-" + tenant.getId().toString().toLowerCase();
            
            
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            String fileId = UUID.randomUUID().toString();
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectName = module.toLowerCase() + "/" + fileId + extension;

            
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

            FileObject fileObject = FileObject.builder()
                    .fileName(originalName)
                    .fileType(file.getContentType())
                    .fileUrl(presignedUrl)
                    .storageKey(objectName)
                    .fileSize(file.getSize())
                    .module(module)
                    .referenceId(referenceId)
                    .tenant(tenant)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return fileRepository.save(fileObject);

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    public List<FileObject> getFiles(UUID tenantId, String module, UUID referenceId) {
        List<FileObject> files = fileRepository.findByTenant_IdAndModuleAndReferenceId(tenantId, module, referenceId);
        
        for (FileObject file : files) {
            String bucketName = "tenant-" + tenantId.toString().toLowerCase();
            String freshUrl = getPresignedUrl(bucketName, file.getStorageKey());
            if (freshUrl != null) {
                file.setFileUrl(freshUrl);
            }
        }
        return files;
    }
    
    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}/{}: {}", bucketName, objectName, e.getMessage());
            return null;
        }
    }
}
