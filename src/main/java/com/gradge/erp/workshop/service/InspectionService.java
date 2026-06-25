package com.gradge.erp.workshop.service;

import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.entity.Inspection;
import com.gradge.erp.workshop.entity.InspectionItem;
import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.enums.InspectionStatus;
import com.gradge.erp.workshop.repository.InspectionItemRepository;
import com.gradge.erp.workshop.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionItemRepository inspectionItemRepository;

    public Inspection createInspection(JobCard jobCard, Tenant tenant) {

        Inspection inspection = Inspection.builder()
                .jobCard(jobCard)
                .tenant(tenant)
                .status(InspectionStatus.PENDING)
                .build();

        return inspectionRepository.save(inspection);
    }

    public List<Inspection> getByJobCard(UUID jobCardId, UUID tenantId) {
        return inspectionRepository.findByJobCard_IdAndTenant_Id(jobCardId, tenantId);
    }

    public Inspection startInspection(UUID id, UUID tenantId) {
        Inspection inspection = inspectionRepository.findByIdAndTenant_Id(id, tenantId);
        inspection.setStatus(InspectionStatus.IN_PROGRESS);
        return inspectionRepository.save(inspection);
    }

    public Inspection completeInspection(UUID id, UUID tenantId, String notes) {
        Inspection inspection = inspectionRepository.findByIdAndTenant_Id(id, tenantId);
        inspection.setStatus(InspectionStatus.COMPLETED);
        inspection.setOverallNotes(notes);
        return inspectionRepository.save(inspection);
    }

    public Inspection approveInspection(UUID id, UUID tenantId) {
        Inspection inspection = inspectionRepository.findByIdAndTenant_Id(id, tenantId);
        inspection.setStatus(InspectionStatus.APPROVED);
        return inspectionRepository.save(inspection);
    }

    public InspectionItem addItem(UUID inspectionId, InspectionItem item) {
        Inspection inspection = inspectionRepository.findById(inspectionId).orElseThrow();
        item.setInspection(inspection);
        return inspectionItemRepository.save(item);
    }

    public List<InspectionItem> getItems(UUID inspectionId) {
        return inspectionItemRepository.findByInspection_Id(inspectionId);
    }
}
