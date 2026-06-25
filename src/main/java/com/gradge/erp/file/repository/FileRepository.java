package com.gradge.erp.file.repository;

import com.gradge.erp.file.entity.FileObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileObject, UUID> {

    List<FileObject> findByTenant_IdAndModuleAndReferenceId(
            UUID tenantId,
            String module,
            UUID referenceId
    );
}
