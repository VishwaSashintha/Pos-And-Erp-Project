package com.gradge.erp.file.entity;

import com.gradge.erp.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_objects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileObject {

    @Id
    @GeneratedValue
    private UUID id;

    private String fileName;

    private String fileType;

    private String fileUrl;

    private String storageKey;

    private Long fileSize;

    private String module; 

    private UUID referenceId; 

    @ManyToOne
    private Tenant tenant;

    private LocalDateTime uploadedAt;
}
