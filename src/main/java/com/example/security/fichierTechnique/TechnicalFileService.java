package com.example.security.fichierTechnique;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.cahierdeCharge.ChargeSheetItemRepository;
import com.example.security.cahierdeCharge.ChargeSheetStatus;
import com.example.security.email.GlobalNotificationService;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistory;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistoryRepository;
import com.example.security.stock.StockModuleRepository;
import com.example.security.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.*;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicalFileService {

    private final TechnicalFileRepository repository;
    private final TechnicalFileItemRepository technicalFileItemRepository;
    private final GlobalNotificationService notificationService;
    private final ChargeSheetItemRepository chargeSheetItemRepository;
    private final TechnicalFileHistoryRepository historyRepository;
    private final StockModuleRepository stockModuleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /*
    =========================
    CREATE TECHNICAL FILE
    =========================
     */

    @Transactional
    public TechnicalFile createTechnicalFile(TechnicalFileDto.CreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Un dossier technique doit contenir au moins un item");
        }

        TechnicalFile technicalFile = new TechnicalFile();
        technicalFile.setReference(dto.getReference());
        technicalFile.setCreatedBy(currentUser.getEmail());
        technicalFile.setCreatedAt(LocalDate.now());

        TechnicalFile savedTechnicalFile = repository.save(technicalFile);

        for (TechnicalFileDto.TechnicalFileItemDto itemDto : dto.getItems()) {

            ChargeSheetItem chargeSheetItem = chargeSheetItemRepository
                    .findById(itemDto.getChargeSheetItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            TechnicalFileItem item = new TechnicalFileItem();

            item.setChargeSheetItem(chargeSheetItem);
            item.setMaintenanceDate(itemDto.getMaintenanceDate());
            item.setTechnicianName(itemDto.getTechnicianName());
            item.setXCode(itemDto.getXCode());
            item.setIndexValue(itemDto.getIndexValue());
            item.setLeoniReferenceNumber(itemDto.getLeoniReferenceNumber());
            item.setProducer(itemDto.getProducer());
            item.setType(itemDto.getType());
            item.setReferencePinePushBack(itemDto.getReferencePinePushBack());

            item.setPosition(itemDto.getPosition());
            item.setPinRigidityM1(itemDto.getPinRigidityM1());
            item.setPinRigidityM2(itemDto.getPinRigidityM2());
            item.setPinRigidityM3(itemDto.getPinRigidityM3());

            item.setDisplacementPathM1(itemDto.getDisplacementPathM1());
            item.setDisplacementPathM2(itemDto.getDisplacementPathM2());
            item.setDisplacementPathM3(itemDto.getDisplacementPathM3());

            item.setMaxSealingValueM1(itemDto.getMaxSealingValueM1());
            item.setMaxSealingValueM2(itemDto.getMaxSealingValueM2());
            item.setMaxSealingValueM3(itemDto.getMaxSealingValueM3());

            item.setProgrammedSealingValueM1(itemDto.getProgrammedSealingValueM1());
            item.setProgrammedSealingValueM2(itemDto.getProgrammedSealingValueM2());
            item.setProgrammedSealingValueM3(itemDto.getProgrammedSealingValueM3());

            item.setDetectionsM1(itemDto.getDetectionsM1());
            item.setDetectionsM2(itemDto.getDetectionsM2());
            item.setDetectionsM3(itemDto.getDetectionsM3());

            item.setRemarks(itemDto.getRemarks());
            item.setCreatedBy(currentUser.getEmail());
            item.setCreatedAt(LocalDate.now());
            item.setValidationStatus(TechnicalFileItemStatus.DRAFT);
            savedTechnicalFile.addTechnicalFileItem(item);
        }

        return repository.save(savedTechnicalFile);
    }
    @Transactional
    public TechnicalFileItem addItemToTechnicalFile(Long technicalFileId, TechnicalFileDto.AddItemDto dto) {
        TechnicalFile tf = repository.findById(technicalFileId)
                .orElseThrow(() -> new RuntimeException("Technical file not found"));

        ChargeSheetItem chargeSheetItem = chargeSheetItemRepository.findById(dto.getChargeSheetItemId())
                .orElseThrow(() -> new RuntimeException("ChargeSheetItem not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        TechnicalFileItem item = new TechnicalFileItem();
        item.setChargeSheetItem(chargeSheetItem);
        item.setMaintenanceDate(dto.getMaintenanceDate());
        item.setTechnicianName(dto.getTechnicianName());
        item.setXCode(dto.getXCode());
        item.setIndexValue(dto.getIndexValue());
        item.setLeoniReferenceNumber(dto.getLeoniReferenceNumber());
        item.setProducer(dto.getProducer());
        item.setType(dto.getType());
        item.setReferencePinePushBack(dto.getReferencePinePushBack());
        item.setPosition(dto.getPosition());
        item.setPinRigidityM1(dto.getPinRigidityM1());
        item.setPinRigidityM2(dto.getPinRigidityM2());
        item.setPinRigidityM3(dto.getPinRigidityM3());
        item.setDisplacementPathM1(dto.getDisplacementPathM1());
        item.setDisplacementPathM2(dto.getDisplacementPathM2());
        item.setDisplacementPathM3(dto.getDisplacementPathM3());
        item.setMaxSealingValueM1(dto.getMaxSealingValueM1());
        item.setMaxSealingValueM2(dto.getMaxSealingValueM2());
        item.setMaxSealingValueM3(dto.getMaxSealingValueM3());
        item.setProgrammedSealingValueM1(dto.getProgrammedSealingValueM1());
        item.setProgrammedSealingValueM2(dto.getProgrammedSealingValueM2());
        item.setProgrammedSealingValueM3(dto.getProgrammedSealingValueM3());
        item.setDetectionsM1(dto.getDetectionsM1());
        item.setDetectionsM2(dto.getDetectionsM2());
        item.setDetectionsM3(dto.getDetectionsM3());
        item.setRemarks(dto.getRemarks());
        item.setValidationStatus(TechnicalFileItemStatus.DRAFT);
        item.setCreatedBy(currentUser.getEmail());
        item.setCreatedAt(LocalDate.now());

        tf.addTechnicalFileItem(item);

        repository.save(tf);

        return item;
    }
    public List<TechnicalFileDto.ListDto> getAllTechnicalFilesList() {
        return repository.findAll().stream()
                .map(tf -> TechnicalFileDto.ListDto.builder()
                        .id(tf.getId())
                        .reference(tf.getReference())
                        .itemCount(tf.getTechnicalFileItems().size())
                        .build())
                .toList();
    }
    public TechnicalFileItem getTechnicalFileItemById(Long itemId) {
        return technicalFileItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Technical file item not found"));
    }
    @Transactional
    public TechnicalFileItem updateTechnicalFileItem(Long itemId, TechnicalFileDto.UpdateItemDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        TechnicalFileItem item = getTechnicalFileItemById(itemId);

        updateItemFields(item, dto, currentUser.getEmail());

        return item;
    }
    @Transactional
    public void deleteTechnicalFileItem(Long itemId) {
        TechnicalFileItem item = getTechnicalFileItemById(itemId);
        technicalFileItemRepository.delete(item);
    }
    public List<Map<String, Object>> getHistoryAudited(Long technicalFileId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(TechnicalFile.class, false, true)
                .add(AuditEntity.id().eq(technicalFileId))
                .getResultList();

        List<Map<String, Object>> historyList = new ArrayList<>();

        for (int i = 0; i < revisions.size(); i++) {
            TechnicalFile entity = (TechnicalFile) revisions.get(i)[0];
            CustomRevisionEntity rev = (CustomRevisionEntity) revisions.get(i)[1];
            RevisionType type = (RevisionType) revisions.get(i)[2];

            if (i > 0) {
                TechnicalFile previousEntity = (TechnicalFile) revisions.get(i-1)[0];

                // Comparer les champs du dossier
                if (!Objects.equals(previousEntity.getReference(), entity.getReference())) {
                    Map<String, Object> changeMap = new HashMap<>();
                    changeMap.put("fieldName", "reference");
                    changeMap.put("oldValue", previousEntity.getReference());
                    changeMap.put("newValue", entity.getReference());
                    changeMap.put("modifiedBy", rev.getUsername());
                    changeMap.put("modifiedAt", new Date(rev.getTimestamp()));
                    changeMap.put("type", type.name());
                    historyList.add(changeMap);
                }
            } else {
                Map<String, Object> creationMap = new HashMap<>();
                creationMap.put("fieldName", "CREATION");
                creationMap.put("oldValue", "-");
                creationMap.put("newValue", "Dossier créé");
                creationMap.put("modifiedBy", rev.getUsername());
                creationMap.put("modifiedAt", new Date(rev.getTimestamp()));
                creationMap.put("type", "ADD");
                historyList.add(creationMap);
            }
        }

        return historyList;
    }

    public List<Map<String, Object>> getItemHistoryAudited(Long itemId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(TechnicalFileItem.class, false, true)
                .add(AuditEntity.id().eq(itemId))
                .getResultList();

        List<Map<String, Object>> historyList = new ArrayList<>();

        // Trier les révisions par date (optionnel)
        revisions.sort((a, b) -> {
            CustomRevisionEntity revA = (CustomRevisionEntity) a[1];
            CustomRevisionEntity revB = (CustomRevisionEntity) b[1];
            return Long.compare(revA.getTimestamp(), revB.getTimestamp());
        });

        for (int i = 0; i < revisions.size(); i++) {
            TechnicalFileItem entity = (TechnicalFileItem) revisions.get(i)[0];
            CustomRevisionEntity rev = (CustomRevisionEntity) revisions.get(i)[1];
            RevisionType type = (RevisionType) revisions.get(i)[2];

            if (i > 0) {
                TechnicalFileItem previousEntity = (TechnicalFileItem) revisions.get(i-1)[0];
                compareFields(historyList, previousEntity, entity, rev, type);
            } else {
                // Première version (création)
                Map<String, Object> creationMap = new HashMap<>();
                creationMap.put("fieldName", "CREATION");
                creationMap.put("oldValue", "-");
                creationMap.put("newValue", "Item créé");
                creationMap.put("modifiedBy", rev.getUsername());
                creationMap.put("modifiedAt", new Date(rev.getTimestamp()));
                creationMap.put("type", "ADD");
                historyList.add(creationMap);
            }
        }

        return historyList;
    }

    private void compareFields(List<Map<String, Object>> historyList,
                               TechnicalFileItem oldItem,
                               TechnicalFileItem newItem,
                               CustomRevisionEntity rev,
                               RevisionType type) {

        // Liste de tous les champs à comparer avec leurs getters
        Map<String, java.util.function.Function<TechnicalFileItem, String>> fieldGetters = new LinkedHashMap<>();

        // ===== CHAMPS DE BASE =====
        fieldGetters.put("maintenanceDate", item -> item.getMaintenanceDate() != null ?
                item.getMaintenanceDate().toString() : null);
        fieldGetters.put("technicianName", TechnicalFileItem::getTechnicianName);
        fieldGetters.put("xCode", TechnicalFileItem::getXCode);
        fieldGetters.put("leoniReferenceNumber", TechnicalFileItem::getLeoniReferenceNumber);
        fieldGetters.put("indexValue", item -> item.getIndexValue() != null ?
                item.getIndexValue().toString() : null);
        fieldGetters.put("producer", TechnicalFileItem::getProducer);
        fieldGetters.put("type", TechnicalFileItem::getType);
        fieldGetters.put("referencePinePushBack", TechnicalFileItem::getReferencePinePushBack);
        fieldGetters.put("position", TechnicalFileItem::getPosition);

        // ===== RAIDURE DES PINS =====
        fieldGetters.put("pinRigidityM1", TechnicalFileItem::getPinRigidityM1);
        fieldGetters.put("pinRigidityM2", TechnicalFileItem::getPinRigidityM2);
        fieldGetters.put("pinRigidityM3", TechnicalFileItem::getPinRigidityM3);

        // ===== DÉPLACEMENT =====
        fieldGetters.put("displacementPathM1", TechnicalFileItem::getDisplacementPathM1);
        fieldGetters.put("displacementPathM2", TechnicalFileItem::getDisplacementPathM2);
        fieldGetters.put("displacementPathM3", TechnicalFileItem::getDisplacementPathM3);

        // ===== MAX SEALING =====
        fieldGetters.put("maxSealingValueM1", TechnicalFileItem::getMaxSealingValueM1);
        fieldGetters.put("maxSealingValueM2", TechnicalFileItem::getMaxSealingValueM2);
        fieldGetters.put("maxSealingValueM3", TechnicalFileItem::getMaxSealingValueM3);

        // ===== PROGRAMMED SEALING =====
        fieldGetters.put("programmedSealingValueM1", TechnicalFileItem::getProgrammedSealingValueM1);
        fieldGetters.put("programmedSealingValueM2", TechnicalFileItem::getProgrammedSealingValueM2);
        fieldGetters.put("programmedSealingValueM3", TechnicalFileItem::getProgrammedSealingValueM3);

        // ===== DÉTECTIONS =====
        fieldGetters.put("detectionsM1", TechnicalFileItem::getDetectionsM1);
        fieldGetters.put("detectionsM2", TechnicalFileItem::getDetectionsM2);
        fieldGetters.put("detectionsM3", TechnicalFileItem::getDetectionsM3);

        // ===== REMARQUES =====
        fieldGetters.put("remarks", TechnicalFileItem::getRemarks);

        // ===== MÉTADONNÉES =====
        fieldGetters.put("createdBy", TechnicalFileItem::getCreatedBy);
        fieldGetters.put("createdAt", item -> item.getCreatedAt() != null ?
                item.getCreatedAt().toString() : null);
        fieldGetters.put("updatedBy", TechnicalFileItem::getUpdatedBy);
        fieldGetters.put("updatedAt", item -> item.getUpdatedAt() != null ?
                item.getUpdatedAt().toString() : null);

        // Parcourir tous les champs et comparer les valeurs
        for (Map.Entry<String, java.util.function.Function<TechnicalFileItem, String>> entry : fieldGetters.entrySet()) {
            String fieldName = entry.getKey();
            String oldVal = entry.getValue().apply(oldItem);
            String newVal = entry.getValue().apply(newItem);

            // Si les valeurs sont différentes, ajouter à l'historique
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> changeMap = new HashMap<>();
                changeMap.put("fieldName", fieldName);
                changeMap.put("oldValue", oldVal != null ? oldVal : "-");
                changeMap.put("newValue", newVal != null ? newVal : "-");
                changeMap.put("modifiedBy", rev.getUsername());
                changeMap.put("modifiedAt", new Date(rev.getTimestamp()));
                changeMap.put("type", type.name());
                historyList.add(changeMap);
            }
        }
    }

    public List<Map<String, Object>> getFullHistoryAudited(Long id) {
        List<Map<String, Object>> fullHistory = new ArrayList<>();

        // Historique du dossier
        List<Map<String, Object>> fileHistory = getHistoryAudited(id);
        fullHistory.addAll(fileHistory);

        // Historique des items
        TechnicalFile tf = getTechnicalFileById(id);
        for (TechnicalFileItem item : tf.getTechnicalFileItems()) {
            List<Map<String, Object>> itemHistory = getItemHistoryAudited(item.getId());
            fullHistory.addAll(itemHistory);
        }

        // Trier par date
        fullHistory.sort((a, b) -> {
            Date dateA = (Date) a.get("modifiedAt");
            Date dateB = (Date) b.get("modifiedAt");
            return dateA.compareTo(dateB);
        });

        return fullHistory;
    }
    // ✅ Historique d'un item
    public List<TechnicalFileHistory> getItemHistory(Long itemId) {
        // Si vous avez un historique spécifique pour les items
        return historyRepository.findByTechnicalFileItemId(itemId);
    }

    /*
    =========================
    UPDATE TECHNICAL FILE
    =========================
     */

    @Transactional
    public TechnicalFile updateTechnicalFile(Long id, TechnicalFileDto.UpdateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        TechnicalFile tf = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technical file not found"));

        String user = currentUser.getEmail();

        if (dto.getReference() != null) {
            saveHistory(id,null,"reference",tf.getReference(),dto.getReference(),user);
            tf.setReference(dto.getReference());
        }

        if (dto.getItems() != null) {

            for (TechnicalFileDto.UpdateItemDto itemDto : dto.getItems()) {

                TechnicalFileItem item = technicalFileItemRepository
                        .findById(itemDto.getTechnicalFileItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found"));

                updateItemFields(item,itemDto,user);
            }
        }

        tf.setUpdatedBy(user);
        tf.setUpdatedAt(LocalDate.now());

        return repository.save(tf);
    }

    /*
    =========================
    UPDATE ITEM FIELDS
    =========================
     */

    private void updateItemFields(TechnicalFileItem item,
                                  TechnicalFileDto.UpdateItemDto dto,
                                  String user) {

        if(dto.getTechnicianName()!=null){
            saveHistory(item.getTechnicalFileId(),item.getId(),
                    "technicianName",item.getTechnicianName(),dto.getTechnicianName(),user);
            item.setTechnicianName(dto.getTechnicianName());
        }

        if(dto.getXCode()!=null){
            saveHistory(item.getTechnicalFileId(),item.getId(),
                    "xCode",item.getXCode(),dto.getXCode(),user);
            item.setXCode(dto.getXCode());
        }

        if(dto.getPosition()!=null){
            saveHistory(item.getTechnicalFileId(),item.getId(),
                    "position",item.getPosition(),dto.getPosition(),user);
            item.setPosition(dto.getPosition());
        }
        if(dto.getIndexValue() != null){
            saveHistory(item.getTechnicalFileId(), item.getId(),
                    "indexValue",
                    item.getIndexValue() != null ? item.getIndexValue().toString() : null,
                    dto.getIndexValue().toString(),
                    user);
            item.setIndexValue(dto.getIndexValue());
        }

        if(dto.getPinRigidityM1()!=null){
            saveHistory(item.getTechnicalFileId(),item.getId(),
                    "pinRigidityM1",item.getPinRigidityM1(),dto.getPinRigidityM1(),user);
            item.setPinRigidityM1(dto.getPinRigidityM1());
        }

        if(dto.getRemarks()!=null){
            saveHistory(item.getTechnicalFileId(),item.getId(),
                    "remarks",item.getRemarks(),dto.getRemarks(),user);
            item.setRemarks(dto.getRemarks());
        }

        item.setUpdatedBy(user);
        item.setUpdatedAt(LocalDate.now());

        technicalFileItemRepository.save(item);
    }

    /*
    =========================
    SAVE HISTORY
    =========================
     */

    private void saveHistory(Long technicalFileId,
                             Long itemId,
                             String field,
                             String oldVal,
                             String newVal,
                             String user){

        if(Objects.equals(oldVal,newVal)) return;

        TechnicalFileHistory h = TechnicalFileHistory.builder()
                .technicalFileId(technicalFileId)
                .technicalFileItemId(itemId)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .modifiedBy(user)
                .modifiedAt(LocalDate.now())
                .build();

        historyRepository.save(h);
    }

    /*
    =========================
    DELETE TECHNICAL FILE
    =========================
     */

    @Transactional
    public void deleteTechnicalFile(Long id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        TechnicalFile tf = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technical file not found"));

        repository.delete(tf);

        notificationService.notifyDocumentDeleted(
                "Dossier Technique",
                id,
                null,
                currentUser.getEmail()
        );
    }

    /*
    =========================
    GET FILE
    =========================
     */

    public TechnicalFile getTechnicalFileById(Long id){

        TechnicalFile tf = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technical file not found"));

        tf.getTechnicalFileItems().size();

        return tf;
    }

    /*
    =========================
    GET ALL FILES
    =========================
     */

    public List<TechnicalFileDto.ResponseDto> getAllTechnicalFilesWithItems(){

        return repository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /*
    =========================
    MAP DTO
    =========================
     */

    private TechnicalFileDto.ResponseDto mapToResponseDto(TechnicalFile tf){
        List<TechnicalFileDto.ResponseItemDto> items =
                tf.getTechnicalFileItems()
                        .stream()
                        .map(item -> TechnicalFileDto.ResponseItemDto.builder()
                                .id(item.getId())
                                .chargeSheetItemId(item.getChargeSheetItem().getId())
                                .itemNumber(item.getChargeSheetItem().getItemNumber())
                                // GÉNÉRAL
                                .position(item.getPosition())
                                .maintenanceDate(item.getMaintenanceDate())
                                .technicianName(item.getTechnicianName())
                                // CODE D'IDENTITÉ
                                .xCode(item.getXCode())
                                .leoniReferenceNumber(item.getLeoniReferenceNumber())
                                .indexValue(item.getIndexValue())
                                .producer(item.getProducer())
                                .type(item.getType())
                                .referencePinePushBack(item.getReferencePinePushBack())
                                // RAIDEUR
                                .pinRigidityM1(item.getPinRigidityM1())
                                .pinRigidityM2(item.getPinRigidityM2())
                                .pinRigidityM3(item.getPinRigidityM3())
                                // DÉPLACEMENT
                                .displacementPathM1(item.getDisplacementPathM1())
                                .displacementPathM2(item.getDisplacementPathM2())
                                .displacementPathM3(item.getDisplacementPathM3())
                                // MAX SEALING
                                .maxSealingValueM1(item.getMaxSealingValueM1())
                                .maxSealingValueM2(item.getMaxSealingValueM2())
                                .maxSealingValueM3(item.getMaxSealingValueM3())
                                // PROGRAMMED SEALING
                                .programmedSealingValueM1(item.getProgrammedSealingValueM1())
                                .programmedSealingValueM2(item.getProgrammedSealingValueM2())
                                .programmedSealingValueM3(item.getProgrammedSealingValueM3())
                                // DÉTECTIONS
                                .detectionsM1(item.getDetectionsM1())
                                .detectionsM2(item.getDetectionsM2())
                                .detectionsM3(item.getDetectionsM3())
                                .remarks(item.getRemarks())
                                // ✅ AJOUTER CES LIGNES
                                .validationStatus(item.getValidationStatus() != null ? item.getValidationStatus().name() : "DRAFT")
                                .validationStatusDisplay(item.getValidationStatus() != null ? item.getValidationStatus().getDisplayName() : "Brouillon")
                                .validatedByPp(item.getValidatedByPp())
                                .validatedAtPp(item.getValidatedAtPp() != null ? item.getValidatedAtPp().toString() : null)
                                .validatedByMc(item.getValidatedByMc())
                                .validatedAtMc(item.getValidatedAtMc() != null ? item.getValidatedAtMc().toString() : null)
                                .validatedByMp(item.getValidatedByMp())
                                .validatedAtMp(item.getValidatedAtMp() != null ? item.getValidatedAtMp().toString() : null)
                                .build())
                        .toList();

        return TechnicalFileDto.ResponseDto.builder()
                .id(tf.getId())
                .reference(tf.getReference())
                .createdBy(tf.getCreatedBy())
                .createdAt(tf.getCreatedAt())
                .updatedBy(tf.getUpdatedBy())
                .updatedAt(tf.getUpdatedAt())
                .items(items)
                .build();
    }

    /*
    =========================
    HISTORY
    =========================
     */

    public List<TechnicalFileHistory> getHistory(Long technicalFileId){
        return historyRepository.findByTechnicalFileId(technicalFileId);
    }
    // ==================== VALIDATION DES ITEMS ====================

    @Transactional
    public TechnicalFileItem validateItem(Long itemId, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        TechnicalFileItem item = getTechnicalFileItemById(itemId);

        TechnicalFileItemStatus currentStatus = item.getValidationStatus();

        // Vérifier si le rôle peut valider ce statut
        if (!currentStatus.canBeValidatedBy(role)) {
            throw new RuntimeException(
                    String.format("Impossible de valider cet item avec le rôle %s (statut actuel: %s)",
                            role, currentStatus.getDisplayName())
            );
        }

        switch (role) {
            case "PP":
                item.setValidationStatus(TechnicalFileItemStatus.VALIDATED_PP);
                item.setValidatedByPp(currentUser.getEmail());
                item.setValidatedAtPp(LocalDateTime.now());
                break;

            case "MC":
                item.setValidationStatus(TechnicalFileItemStatus.VALIDATED_MC);
                item.setValidatedByMc(currentUser.getEmail());
                item.setValidatedAtMc(LocalDateTime.now());
                break;

            case "MP":
                item.setValidationStatus(TechnicalFileItemStatus.VALIDATED_MP);
                item.setValidatedByMp(currentUser.getEmail());
                item.setValidatedAtMp(LocalDateTime.now());
                break;

            default:
                throw new RuntimeException("Rôle non reconnu pour la validation: " + role);
        }

        item.setUpdatedBy(currentUser.getEmail());
        item.setUpdatedAt(LocalDate.now());

        // Sauvegarder l'historique
        saveHistory(
                item.getTechnicalFileId(),
                item.getId(),
                "validationStatus",
                currentStatus.name(),
                item.getValidationStatus().name(),
                currentUser.getEmail()
        );

        return technicalFileItemRepository.save(item);
    }

    // Vérifier si un utilisateur peut valider un item
    public boolean canValidateItem(Long itemId, String role) {
        TechnicalFileItem item = getTechnicalFileItemById(itemId);
        return item.getValidationStatus().canBeValidatedBy(role);
    }

}