package com.gradge.erp.workshop.repository;

import com.gradge.erp.workshop.entity.InspectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, UUID> {

    List<InspectionItem> findByInspection_Id(UUID inspectionId);
}
