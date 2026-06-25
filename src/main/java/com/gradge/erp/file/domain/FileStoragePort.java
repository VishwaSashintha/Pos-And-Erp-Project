package com.gradge.erp.file.domain;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {

    String upload(MultipartFile file);

}