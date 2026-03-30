package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.cahierdeCharge.ChargeSheetItemRepository;
import com.example.security.cahierdeCharge.ChargeSheetService;
import com.example.security.cahierdeCharge.ChargeSheetStatus;
import com.example.security.email.GlobalNotificationService;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRepository repository;
    private final GlobalNotificationService notificationService;
    private final ChargeSheetItemRepository chargeSheetItemRepository;

    @Transactional
    public Compliance createCompliance(ComplianceDto.CreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // ⚡ Récupérer l’item
        ChargeSheetItem item = chargeSheetItemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        // ⚡ Récupérer le chargeSheetId depuis l'item
        Long chargeSheetId = item.getChargeSheet().getId();
        Compliance compliance = Compliance.builder()
                .item(item) // ✅ assigné ici
                .chargeSheetId(chargeSheetId)
                .orderNumber(dto.getOrderNumber())
                .orderitemNumber(dto.getOrderitemNumber())
                .testDateTime(dto.getTestDateTime())
                .technicianName(dto.getTechnicianName())
                .rfidNumber(dto.getRfidNumber())
                .leoniPartNumber(dto.getLeoniPartNumber())
                .indexValue(dto.getIndexValue())
                .producer(dto.getProducer())
                .type(dto.getType())
                .sequenceTestPins(dto.getSequenceTestPins())
                .codingRequest(dto.getCodingRequest())
                .secondaryLocking(dto.getSecondaryLocking())
                .offsetTestMm(dto.getOffsetTestMm())
                .stableOffsetTestMm(dto.getStableOffsetTestMm())
                .displacementPathPushBackMm(dto.getDisplacementPathPushBackMm())
                .housingAttachments(dto.getHousingAttachments())
                .maxLeakTestMbar(dto.getMaxLeakTestMbar())
                .adjustmentLeakTestMbar(dto.getAdjustmentLeakTestMbar())
                .colourVerification(dto.getColourVerification())
                .terminalAlignment(dto.getTerminalAlignment())
                .openShuntsAirbag(dto.getOpenShuntsAirbag())
                .spacerClosingUnit(dto.getSpacerClosingUnit())
                .specialFunctions(dto.getSpecialFunctions())
                .contactProblemsPercentage(dto.getContactProblemsPercentage())
                .qualifiedTestModule(dto.getQualifiedTestModule())
                .conditionallyQualifiedTestModule(dto.getConditionallyQualifiedTestModule())
                .notQualifiedTestModule(dto.getNotQualifiedTestModule())
                .remarks(dto.getRemarks())
                .createdBy(currentUser.getEmail())
                .createdAt(LocalDate.now())
                .build();

        Compliance saved = repository.save(compliance);

        notificationService.notifyComplianceCreated(
                saved.getId(),
                saved.getChargeSheetId(),
                currentUser.getEmail()
        );

        return saved;
    }


    @Transactional
    public Compliance updateCompliance(Long id, ComplianceDto.UpdateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Compliance compliance = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));

        // Update inspection fields
        compliance.setOrderitemNumber(dto.getOrderitemNumber());
        compliance.setSequenceTestPins(dto.getSequenceTestPins());
        compliance.setCodingRequest(dto.getCodingRequest());
        compliance.setSecondaryLocking(dto.getSecondaryLocking());
        compliance.setOffsetTestMm(dto.getOffsetTestMm());
        compliance.setStableOffsetTestMm(dto.getStableOffsetTestMm());
        compliance.setDisplacementPathPushBackMm(dto.getDisplacementPathPushBackMm());
        compliance.setHousingAttachments(dto.getHousingAttachments());
        compliance.setMaxLeakTestMbar(dto.getMaxLeakTestMbar());
        compliance.setAdjustmentLeakTestMbar(dto.getAdjustmentLeakTestMbar());
        compliance.setColourVerification(dto.getColourVerification());
        compliance.setTerminalAlignment(dto.getTerminalAlignment());
        compliance.setOpenShuntsAirbag(dto.getOpenShuntsAirbag());
        compliance.setSpacerClosingUnit(dto.getSpacerClosingUnit());
        compliance.setSpecialFunctions(dto.getSpecialFunctions());
        compliance.setContactProblemsPercentage(dto.getContactProblemsPercentage());

        // Update qualification result
        compliance.setQualifiedTestModule(dto.getQualifiedTestModule());
        compliance.setConditionallyQualifiedTestModule(dto.getConditionallyQualifiedTestModule());
        compliance.setNotQualifiedTestModule(dto.getNotQualifiedTestModule());

        // Update remarks and status
        compliance.setRemarks(dto.getRemarks());


        compliance.setUpdatedBy(currentUser.getEmail());
        compliance.setUpdatedAt(LocalDate.now());

        Compliance updated = repository.save(compliance);

        // Notification à TOUS les utilisateurs
        notificationService.notifyComplianceUpdated(
                updated.getId(),
                updated.getChargeSheetId(),
                currentUser.getEmail()
        );

        return updated;
    }

    @Transactional
    public void deleteCompliance(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Compliance compliance = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));

        Long chargeSheetId = compliance.getChargeSheetId();
        repository.deleteById(id);

        // Notification à TOUS les utilisateurs
        notificationService.notifyDocumentDeleted(
                "Fiche de Conformité",
                id,
                chargeSheetId,
                currentUser.getEmail()
        );
    }

    public List<Compliance> getComplianceByChargeSheetId(Long chargeSheetId) {
        return repository.findByChargeSheetId(chargeSheetId);
    }

    public Compliance getComplianceById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));
    }

    public List<Compliance> getAllCompliance() {
        return repository.findAll();
    }
    public List<ComplianceDisplayDto> getAllComplianceForDisplay() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProject = currentUser.getProjet();

        List<Compliance> list;

        switch (userRole) {

            case "ADMIN":
                list = repository.findAll();
                break;

            case "ING":
                list = repository.findByProject(userProject);
                break;

            case "PT":
                list = repository.findByProjectAndChargeSheetStatusIn(
                        userProject,
                        List.of(
                                ChargeSheetStatus.COMPLETED
                        )
                );
                break;

            case "PP":
                list = repository.findByProjectAndChargeSheetStatusIn(
                        userProject,
                        List.of(
                                ChargeSheetStatus.COMPLETED
                        )
                );
                break;

            case "MC":
            case "MP":
                list = repository.findByProjectAndChargeSheetStatus(
                        userProject,
                        ChargeSheetStatus.COMPLETED
                );
                break;

            default:
                list = List.of();
        }

        return list.stream().map(c -> ComplianceDisplayDto.builder()
                .id(c.getId())
                .chargeSheetId(c.getChargeSheetId())
                .orderNumber(c.getOrderNumber())
                .itemNumber(c.getIndexValue())
                .orderitemNumber(c.getOrderitemNumber())
                .testDateTime(c.getTestDateTime())
                .technicianName(c.getTechnicianName())
                .rfidNumber(c.getRfidNumber())
                .leoniPartNumber(c.getLeoniPartNumber())
                .producer(c.getProducer())
                .indexValue(c.getIndexValue())
                .type(c.getType())
                .sequenceTestPins(c.getSequenceTestPins())
                .codingRequest(c.getCodingRequest())
                .secondaryLocking(c.getSecondaryLocking())
                .offsetTestMm(c.getOffsetTestMm())
                .stableOffsetTestMm(c.getStableOffsetTestMm())
                .displacementPathPushBackMm(c.getDisplacementPathPushBackMm())
                .housingAttachments(c.getHousingAttachments())
                .maxLeakTestMbar(c.getMaxLeakTestMbar())
                .adjustmentLeakTestMbar(c.getAdjustmentLeakTestMbar())
                .colourVerification(c.getColourVerification())
                .terminalAlignment(c.getTerminalAlignment())
                .openShuntsAirbag(c.getOpenShuntsAirbag())
                .spacerClosingUnit(c.getSpacerClosingUnit())
                .specialFunctions(c.getSpecialFunctions())
                .contactProblemsPercentage(c.getContactProblemsPercentage())
                .qualifiedTestModule(c.getQualifiedTestModule())
                .conditionallyQualifiedTestModule(c.getConditionallyQualifiedTestModule())
                .notQualifiedTestModule(c.getNotQualifiedTestModule())
                .remarks(c.getRemarks())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedBy(c.getUpdatedBy())
                .updatedAt(c.getUpdatedAt())
                .build()
        ).toList();
    }
    @Transactional
    public List<Compliance> createComplianceForItem(ChargeSheetItem item) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        List<Compliance> createdList = new ArrayList<>();

        for (int i = 1; i <= item.getQuantityOfTestModules(); i++) {
            Compliance compliance = Compliance.builder()
                    .chargeSheetId(item.getChargeSheet().getId())
                    .item(item)
                    .orderNumber(item.getChargeSheet().getOrderNumber()) // récupère order number du cahier
                    .indexValue(i) // chaque module a son index
                    .createdBy(currentUser.getEmail())
                    .createdAt(LocalDate.now())
                    .build();

            createdList.add(repository.save(compliance));
        }

        return createdList;
    }


}