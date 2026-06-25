package com.gradge.erp.file.controller;

import com.gradge.erp.file.entity.FileObject;
import com.gradge.erp.file.service.FileStorageService;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public FileObject upload(
            @RequestParam MultipartFile file,
            @RequestParam String module,
            @RequestParam UUID referenceId,
            @RequestHeader("tenantId") UUID tenantId
    ) throws IOException {

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        return fileStorageService.uploadFile(file, module, referenceId, tenant);
    }

    @GetMapping("/{tenantId}")
    public List<FileObject> getFiles(
            @PathVariable("tenantId") UUID tenantId,
            @RequestParam String module,
            @RequestParam UUID referenceId
    ) {
        return fileStorageService.getFiles(tenantId, module, referenceId);
    }
}
