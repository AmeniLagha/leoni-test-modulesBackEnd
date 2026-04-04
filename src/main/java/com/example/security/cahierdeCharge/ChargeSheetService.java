package com.example.security.cahierdeCharge;

import com.example.security.email.GlobalNotificationService;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeSheetService {

    private final ChargeSheetRepository repository;
    private final ChargeSheetItemRepository itemRepository;
    private final GlobalNotificationService notificationService;
    private final UserRepository userRepository;
    // Ajoutez cette dépendance
    private final ReceptionHistoryRepository receptionHistoryRepository;

    @Transactional
    public ChargeSheet createChargeSheet(ChargeSheetDto.CreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = ChargeSheet.builder()
                .plant(dto.getPlant())
                .project(dto.getProject())
                .harnessRef(dto.getHarnessRef())
                .issuedBy(dto.getIssuedBy())
                .emailAddress(dto.getEmailAddress())
                .phoneNumber(dto.getPhoneNumber())
                .orderNumber(dto.getOrderNumber())
                .costCenterNumber(dto.getCostCenterNumber())
                .date(dto.getDate())
                .preferredDeliveryDate(dto.getPreferredDeliveryDate())
                .status(ChargeSheetStatus.DRAFT)
                .createdBy(currentUser.getEmail())
                .createdAt(LocalDate.now())
                .build();

        ChargeSheet savedSheet = repository.save(chargeSheet);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ChargeSheetDto.ItemDto itemDto : dto.getItems()) {
                ChargeSheetItem item = mapToItem(itemDto, savedSheet, currentUser);
                savedSheet.addItem(item);
            }
        } else {
            ChargeSheetItem defaultItem = createDefaultItem(savedSheet, currentUser);
            savedSheet.addItem(defaultItem);
        }

        ChargeSheet finalSheet = repository.save(savedSheet);
        // Envoi de la notification sous forme de tableau HTML
        notificationService.notifyChargeSheetCreatedDetailed(finalSheet);

        return finalSheet;
    }

    @Transactional
    public ChargeSheetItem updateTechnicalFields(Long sheetId, Long itemId, ChargeSheetDto.UpdateTechDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getChargeSheet().getId().equals(sheetId)) {
            throw new RuntimeException("Item does not belong to this charge sheet");
        }

        updateItemFromDto(item, dto);

        item.setUpdatedBy(currentUser.getEmail());
        item.setUpdatedAt(LocalDate.now());
        item.setItemStatus("TECH_FILLED");

        ChargeSheetItem updated = itemRepository.save(item);

        // ✅ METTRE À JOUR LE STATUT GLOBAL ICI
        updateGlobalStatusAfterItemUpdate(item.getChargeSheet());

        notificationService.notifyChargeSheetUpdated(
                sheetId,
                "Item " + item.getItemNumber() + " modifié",
                currentUser.getEmail(),
                "PT"
        );

        return updated;
    }
    private void updateGlobalStatusAfterItemUpdate(ChargeSheet sheet) {
        // Vérifier si tous les items sont TECH_FILLED
        boolean allItemsFilled = sheet.getItems().stream()
                .allMatch(item -> "TECH_FILLED".equals(item.getItemStatus()));

        // Si tous les items sont remplis
        if (allItemsFilled) {
            ChargeSheetStatus currentStatus = sheet.getStatus();

            if (currentStatus == ChargeSheetStatus.DRAFT) {
                // DRAFT -> TECH_FILLED (les items sont remplis mais ING n'a pas encore validé)
                sheet.setStatus(ChargeSheetStatus.TECH_FILLED);
                repository.save(sheet);
            } else if (currentStatus == ChargeSheetStatus.VALIDATED_ING) {
                // ✅ Si ING a déjà validé et tous les items sont remplis
                // On passe à TECH_FILLED (en attendant validation PT)
                sheet.setStatus(ChargeSheetStatus.TECH_FILLED);
                repository.save(sheet);
            }
        }
    }

    @Transactional
    public ChargeSheet addItem(Long sheetId, ChargeSheetDto.ItemDto itemDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        ChargeSheetItem item = mapToItem(itemDto, chargeSheet, currentUser);
        chargeSheet.addItem(item);

        ChargeSheet updated = repository.save(chargeSheet);

        notificationService.notifyChargeSheetUpdated(
                sheetId,
                "Nouvel item ajouté: " + item.getItemNumber(),
                currentUser.getEmail(),
                "ING"
        );

        return updated;
    }

    @Transactional
    public void removeItem(Long sheetId, Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getChargeSheet().getId().equals(sheetId)) {
            throw new RuntimeException("Item does not belong to this charge sheet");
        }

        chargeSheet.removeItem(item);
        itemRepository.delete(item);
        repository.save(chargeSheet);

        // CORRECTION: Le troisième paramètre doit être Long, pas String
        notificationService.notifyDocumentDeleted(
                "Item de Cahier des Charges",
                itemId,                    // Long - OK
                sheetId,                   // Long - CORRIGÉ (au lieu de "Sheet ID: " + sheetId)
                currentUser.getEmail()
        );
    }

    public ChargeSheet getChargeSheetById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));
    }

    public ChargeSheetDto.CompleteDto getChargeSheetComplete(Long id) {
        ChargeSheet chargeSheet = getChargeSheetById(id);
        return mapToCompleteDto(chargeSheet);
    }

    public List<ChargeSheetDto.CompleteDto> getAllChargeSheets() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProject = currentUser.getProjet();

        List<ChargeSheet> sheets;

        switch (userRole) {
            case "ADMIN":
                // Admin voit tout
                sheets = repository.findAll();
                break;

            case "ING":
                // ING voit tous les cahiers de son projet (même non validés)
                sheets = repository.findByProject(userProject);
                break;

            case "PT":
                // PT voit seulement les cahiers validés par ING
                sheets = repository.findByProjectAndStatusIn(
                        userProject,
                        List.of(
                                ChargeSheetStatus.VALIDATED_ING,
                                ChargeSheetStatus.TECH_FILLED,
                                ChargeSheetStatus.VALIDATED_PT,
                                ChargeSheetStatus.SENT_TO_SUPPLIER,
                                ChargeSheetStatus.COMPLETED
                        )
                );
                break;

            case "PP":
                // PP voit les cahiers validés par PT et COMPLETED
                sheets = repository.findByProjectAndStatusIn(
                        userProject,
                        List.of(
                                ChargeSheetStatus.VALIDATED_PT,
                                ChargeSheetStatus.SENT_TO_SUPPLIER,
                                ChargeSheetStatus.COMPLETED
                        )
                );
                break;

            case "MC":
            case "MP":
                // MC et MP voient seulement les cahiers complétés
                sheets = repository.findByProjectAndStatus(
                        userProject,
                        ChargeSheetStatus.COMPLETED
                );
                break;

            default:
                // Par défaut, rien
                sheets = List.of();
                break;
        }

        return sheets.stream()
                .map(this::mapToCompleteDto)
                .collect(Collectors.toList());
    }


    public ChargeSheet save(ChargeSheet chargeSheet) {
        return repository.save(chargeSheet);
    }

    public ChargeSheetDto.ItemDto mapToItemDtoPublic(ChargeSheetItem item) {
        return mapToItemDto(item);
    }

    // === Méthodes privées ===

    private ChargeSheetItem mapToItem(ChargeSheetDto.ItemDto dto, ChargeSheet sheet, User user) {
        return ChargeSheetItem.builder()
                .chargeSheet(sheet)
                .itemNumber(dto.getItemNumber())
                .samplesExist(dto.getSamplesExist())
                .ways(dto.getWays())
                .housingColour(dto.getHousingColour())
                .testModuleExistInDatabase(dto.getTestModuleExistInDatabase())
                .housingReferenceLeoni(dto.getHousingReferenceLeoni())
                .housingReferenceSupplierCustomer(dto.getHousingReferenceSupplierCustomer())
                .referenceSealsClipsCableTiesCap(dto.getReferenceSealsClipsCableTiesCap())
                .realConnectorPicture(dto.getRealConnectorPicture())
                .quantityOfTestModules(dto.getQuantityOfTestModules())
                .itemStatus("DRAFT")
                .createdBy(user.getEmail())
                .createdAt(LocalDate.now())
                .build();
    }

    private ChargeSheetItem createDefaultItem(ChargeSheet sheet, User user) {
        return ChargeSheetItem.builder()
                .chargeSheet(sheet)
                .itemNumber("1")
                .samplesExist("No")
                .quantityOfTestModules(1)
                .itemStatus("DRAFT")
                .createdBy(user.getEmail())
                .createdAt(LocalDate.now())
                .build();
    }

    private void updateItemFromDto(ChargeSheetItem item, ChargeSheetDto.UpdateTechDto dto) {
        item.setOutsideHousingExist(dto.getOutsideHousingExist());
        item.setInsideHousingExist(dto.getInsideHousingExist());
        item.setMechanicalCoding(dto.getMechanicalCoding());
        item.setElectricalCoding(dto.getElectricalCoding());
        item.setCpaExistOpen(dto.getCpaExistOpen());
        item.setCpaExistClosed(dto.getCpaExistClosed());
        item.setCoverHoodExist(dto.getCoverHoodExist());
        item.setCoverHoodClosed(dto.getCoverHoodClosed());
        item.setCapExist(dto.getCapExist());
        item.setBayonetCapExist(dto.getBayonetCapExist());
        item.setBracketExist(dto.getBracketExist());
        item.setBracketOpen(dto.getBracketOpen());
        item.setBracketClosed(dto.getBracketClosed());
        item.setLatchWingExist(dto.getLatchWingExist());
        item.setSliderExist(dto.getSliderExist());
        item.setSliderOpen(dto.getSliderOpen());
        item.setSliderClosed(dto.getSliderClosed());
        item.setSecondaryLockExist(dto.getSecondaryLockExist());
        item.setSecondaryLockOpen(dto.getSecondaryLockOpen());
        item.setSecondaryLockClosed(dto.getSecondaryLockClosed());
        item.setOffsetTest(dto.getOffsetTest());
        item.setPushBackTest(dto.getPushBackTest());
        item.setTerminalOrientation(dto.getTerminalOrientation());
        item.setTerminalDifferentiation(dto.getTerminalDifferentiation());
        item.setAirbagTestViaServiceWindow(dto.getAirbagTestViaServiceWindow());
        item.setLeakTestPressure(dto.getLeakTestPressure());
        item.setLeakTestVacuum(dto.getLeakTestVacuum());
        item.setSealExist(dto.getSealExist());
        item.setCableTieExist(dto.getCableTieExist());
        item.setCableTieLeft(dto.getCableTieLeft());
        item.setCableTieRight(dto.getCableTieRight());
        item.setCableTieMiddle(dto.getCableTieMiddle());
        item.setCableTieLeftRight(dto.getCableTieLeftRight());
        item.setClipExist(dto.getClipExist());
        item.setScrewExist(dto.getScrewExist());
        item.setNutExist(dto.getNutExist());
        item.setConvolutedConduitExist(dto.getConvolutedConduitExist());
        item.setConvolutedConduitClosed(dto.getConvolutedConduitClosed());
        item.setAntennaOnlyPresenceTest(dto.getAntennaOnlyPresenceTest());
        item.setAntennaOnlyContactingOfShield(dto.getAntennaOnlyContactingOfShield());
        item.setAntennaContactingOfShieldAndCoreWire(dto.getAntennaContactingOfShieldAndCoreWire());
        item.setRingTerminal(dto.getRingTerminal());
        item.setDiameterInside(dto.getDiameterInside());
        item.setDiameterOutside(dto.getDiameterOutside());
        item.setSingleContact(dto.getSingleContact());
        item.setHeatShrinkExist(dto.getHeatShrinkExist());
        item.setOpenShuntsAirbag(dto.getOpenShuntsAirbag());
        item.setFlowTest(dto.getFlowTest());
        item.setSolidMetalContour(dto.getSolidMetalContour());
        item.setMetalContourAdjustable(dto.getMetalContourAdjustable());
        item.setGrommetExist(dto.getGrommetExist());
        item.setGrommetOrientation(dto.getGrommetOrientation());
        item.setCableChannelExist(dto.getCableChannelExist());
        item.setCableChannelClosed(dto.getCableChannelClosed());
        item.setColourDetectionPrepared(dto.getColourDetectionPrepared());
        item.setExtraLED(dto.getExtraLED());
        item.setSpring(dto.getSpring());
        item.setOtherDetection(dto.getOtherDetection());
        item.setSpacerClosingUnit(dto.getSpacerClosingUnit());
        item.setLeakTestComplex(dto.getLeakTestComplex());
        item.setPinStraightnessCheck(dto.getPinStraightnessCheck());
        item.setPresenceTestOfOneSideConnectedShield(dto.getPresenceTestOfOneSideConnectedShield());
        item.setContrastDetectionGreyValueSensor(dto.getContrastDetectionGreyValueSensor());
        item.setColourDetection(dto.getColourDetection());
        item.setAttenuationWithModeScrambler(dto.getAttenuationWithModeScrambler());
        item.setAttenuationWithoutModeScrambler(dto.getAttenuationWithoutModeScrambler());
        item.setInsulationResistance(dto.getInsulationResistance());
        item.setHighVoltageModule(dto.getHighVoltageModule());
        item.setKelvinMeasurementHV(dto.getKelvinMeasurementHV());
        item.setActuatorTestHV(dto.getActuatorTestHV());
        item.setChargingSystemElectrical(dto.getChargingSystemElectrical());
        item.setPtuPipeTestUnit(dto.getPtuPipeTestUnit());
        item.setGtuGrommetTestUnit(dto.getGtuGrommetTestUnit());
        item.setLedLEDTestModule(dto.getLedLEDTestModule());
        item.setTigTerminalInsertionGuidance(dto.getTigTerminalInsertionGuidance());
        item.setLinBusFunctionalityTest(dto.getLinBusFunctionalityTest());
        item.setCanBusFunctionalityTest(dto.getCanBusFunctionalityTest());
        item.setEsdConformModule(dto.getEsdConformModule());
        item.setFixedBlock(dto.getFixedBlock());
        item.setMovingBlock(dto.getMovingBlock());
        item.setTiltModule(dto.getTiltModule());
        item.setSlideModule(dto.getSlideModule());
        item.setHandAdapter(dto.getHandAdapter());
        item.setLsmLeoniSmartModule(dto.getLsmLeoniSmartModule());
        item.setLeoniStandardTestTable(dto.getLeoniStandardTestTable());
        item.setMetalRailsFasteningSystem(dto.getMetalRailsFasteningSystem());
        item.setMetalPlatesFasteningSystem(dto.getMetalPlatesFasteningSystem());
        item.setQuickConnectionByCanonConnector(dto.getQuickConnectionByCanonConnector());
        item.setTestBoard(dto.getTestBoard());
        item.setWeetech(dto.getWeetech());
        item.setBak(dto.getBak());
        item.setOgc(dto.getOgc());
        item.setAdaptronicHighVoltage(dto.getAdaptronicHighVoltage());
        item.setEmdepHVBananaPlug(dto.getEmdepHVBananaPlug());
        item.setLeoniEMOStandardHV(dto.getLeoniEMOStandardHV());
        item.setClipOrientation(dto.getClipOrientation());
        item.setUnitPrice(dto.getUnitPrice());
        item.setTotalPrice(dto.getTotalPrice());
    }


    private ChargeSheetDto.CompleteDto mapToCompleteDto(ChargeSheet sheet) {
        List<ChargeSheetDto.ItemDto> itemDtos = sheet.getItems().stream()
                .map(this::mapToItemDto)
                .collect(Collectors.toList());

        return ChargeSheetDto.CompleteDto.builder()
                .id(sheet.getId())
                .plant(sheet.getPlant())
                .project(sheet.getProject())
                .harnessRef(sheet.getHarnessRef())
                .issuedBy(sheet.getIssuedBy())
                .emailAddress(sheet.getEmailAddress())
                .phoneNumber(sheet.getPhoneNumber())
                .orderNumber(sheet.getOrderNumber())
                .costCenterNumber(sheet.getCostCenterNumber())
                .date(sheet.getDate())
                .preferredDeliveryDate(sheet.getPreferredDeliveryDate())
                .items(itemDtos)
                .status(sheet.getStatus())
                .createdBy(sheet.getCreatedBy())
                .createdAt(sheet.getCreatedAt())
                .updatedBy(sheet.getUpdatedBy())
                .updatedAt(sheet.getUpdatedAt())
                .build();
    }

    private ChargeSheetDto.ItemDto mapToItemDto(ChargeSheetItem item) {
        return ChargeSheetDto.ItemDto.builder()
                .id(item.getId())
                .itemNumber(item.getItemNumber())
                .samplesExist(item.getSamplesExist())
                .ways(item.getWays())
                .housingColour(item.getHousingColour())
                .testModuleExistInDatabase(item.getTestModuleExistInDatabase())
                .housingReferenceLeoni(item.getHousingReferenceLeoni())
                .housingReferenceSupplierCustomer(item.getHousingReferenceSupplierCustomer())
                .referenceSealsClipsCableTiesCap(item.getReferenceSealsClipsCableTiesCap())
                .realConnectorPicture(item.getRealConnectorPicture())
                .quantityOfTestModules(item.getQuantityOfTestModules())
                .outsideHousingExist(item.getOutsideHousingExist())
                .insideHousingExist(item.getInsideHousingExist())
                .mechanicalCoding(item.getMechanicalCoding())
                .electricalCoding(item.getElectricalCoding())
                .cpaExistOpen(item.getCpaExistOpen())
                .cpaExistClosed(item.getCpaExistClosed())
                .coverHoodExist(item.getCoverHoodExist())
                .coverHoodClosed(item.getCoverHoodClosed())
                .capExist(item.getCapExist())
                .bayonetCapExist(item.getBayonetCapExist())
                .bracketExist(item.getBracketExist())
                .bracketOpen(item.getBracketOpen())
                .bracketClosed(item.getBracketClosed())
                .latchWingExist(item.getLatchWingExist())
                .sliderExist(item.getSliderExist())
                .sliderOpen(item.getSliderOpen())
                .sliderClosed(item.getSliderClosed())
                .secondaryLockExist(item.getSecondaryLockExist())
                .secondaryLockOpen(item.getSecondaryLockOpen())
                .secondaryLockClosed(item.getSecondaryLockClosed())
                .offsetTest(item.getOffsetTest())
                .pushBackTest(item.getPushBackTest())
                .terminalOrientation(item.getTerminalOrientation())
                .terminalDifferentiation(item.getTerminalDifferentiation())
                .airbagTestViaServiceWindow(item.getAirbagTestViaServiceWindow())
                .leakTestPressure(item.getLeakTestPressure())
                .leakTestVacuum(item.getLeakTestVacuum())
                .sealExist(item.getSealExist())
                .cableTieExist(item.getCableTieExist())
                .cableTieLeft(item.getCableTieLeft())
                .cableTieRight(item.getCableTieRight())
                .cableTieMiddle(item.getCableTieMiddle())
                .cableTieLeftRight(item.getCableTieLeftRight())
                .clipExist(item.getClipExist())
                .screwExist(item.getScrewExist())
                .nutExist(item.getNutExist())
                .convolutedConduitExist(item.getConvolutedConduitExist())
                .convolutedConduitClosed(item.getConvolutedConduitClosed())
                .antennaOnlyPresenceTest(item.getAntennaOnlyPresenceTest())
                .antennaOnlyContactingOfShield(item.getAntennaOnlyContactingOfShield())
                .antennaContactingOfShieldAndCoreWire(item.getAntennaContactingOfShieldAndCoreWire())
                .ringTerminal(item.getRingTerminal())
                .diameterInside(item.getDiameterInside())
                .diameterOutside(item.getDiameterOutside())
                .singleContact(item.getSingleContact())
                .heatShrinkExist(item.getHeatShrinkExist())
                .openShuntsAirbag(item.getOpenShuntsAirbag())
                .flowTest(item.getFlowTest())
                .solidMetalContour(item.getSolidMetalContour())
                .metalContourAdjustable(item.getMetalContourAdjustable())
                .grommetExist(item.getGrommetExist())
                .grommetOrientation(item.getGrommetOrientation())
                .cableChannelExist(item.getCableChannelExist())
                .cableChannelClosed(item.getCableChannelClosed())
                .colourDetectionPrepared(item.getColourDetectionPrepared())
                .extraLED(item.getExtraLED())
                .spring(item.getSpring())
                .otherDetection(item.getOtherDetection())
                .spacerClosingUnit(item.getSpacerClosingUnit())
                .leakTestComplex(item.getLeakTestComplex())
                .pinStraightnessCheck(item.getPinStraightnessCheck())
                .presenceTestOfOneSideConnectedShield(item.getPresenceTestOfOneSideConnectedShield())
                .contrastDetectionGreyValueSensor(item.getContrastDetectionGreyValueSensor())
                .colourDetection(item.getColourDetection())
                .attenuationWithModeScrambler(item.getAttenuationWithModeScrambler())
                .attenuationWithoutModeScrambler(item.getAttenuationWithoutModeScrambler())
                .insulationResistance(item.getInsulationResistance())
                .highVoltageModule(item.getHighVoltageModule())
                .kelvinMeasurementHV(item.getKelvinMeasurementHV())
                .actuatorTestHV(item.getActuatorTestHV())
                .chargingSystemElectrical(item.getChargingSystemElectrical())
                .ptuPipeTestUnit(item.getPtuPipeTestUnit())
                .gtuGrommetTestUnit(item.getGtuGrommetTestUnit())
                .ledLEDTestModule(item.getLedLEDTestModule())
                .tigTerminalInsertionGuidance(item.getTigTerminalInsertionGuidance())
                .linBusFunctionalityTest(item.getLinBusFunctionalityTest())
                .canBusFunctionalityTest(item.getCanBusFunctionalityTest())
                .esdConformModule(item.getEsdConformModule())
                .fixedBlock(item.getFixedBlock())
                .movingBlock(item.getMovingBlock())
                .tiltModule(item.getTiltModule())
                .slideModule(item.getSlideModule())
                .handAdapter(item.getHandAdapter())
                .lsmLeoniSmartModule(item.getLsmLeoniSmartModule())
                .leoniStandardTestTable(item.getLeoniStandardTestTable())
                .metalRailsFasteningSystem(item.getMetalRailsFasteningSystem())
                .metalPlatesFasteningSystem(item.getMetalPlatesFasteningSystem())
                .quickConnectionByCanonConnector(item.getQuickConnectionByCanonConnector())
                .testBoard(item.getTestBoard())
                .weetech(item.getWeetech())
                .bak(item.getBak())
                .ogc(item.getOgc())
                .adaptronicHighVoltage(item.getAdaptronicHighVoltage())
                .emdepHVBananaPlug(item.getEmdepHVBananaPlug())
                .leoniEMOStandardHV(item.getLeoniEMOStandardHV())
                .clipOrientation(item.getClipOrientation())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .itemStatus(item.getItemStatus())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedBy(item.getUpdatedBy())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    @Transactional
    public void deleteChargeSheet(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found with id: " + id));

        // Supprimer le cahier (les items seront supprimés automatiquement grâce à cascade)
        repository.delete(chargeSheet);

        // Notification
        notificationService.notifyDocumentDeleted(
                "Cahier des Charges",
                id,
                null,
                currentUser.getEmail()
        );
    }
    @Transactional
    public ChargeSheet validateByIng(Long sheetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que l'utilisateur est ING
        if (!currentUser.getRole().name().equals("ING") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul ING peut valider cette étape");
        }

        // Vérifier que le statut actuel est DRAFT
        if (chargeSheet.getStatus() != ChargeSheetStatus.DRAFT) {
            throw new RuntimeException("Le cahier doit être en mode DRAFT pour être validé par ING");
        }

        chargeSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        chargeSheet.setUpdatedBy(currentUser.getEmail());
        chargeSheet.setUpdatedAt(LocalDate.now());

        ChargeSheet validated = repository.save(chargeSheet);

        // Notification
        notificationService.notifyChargeSheetUpdated(
                sheetId,
                "Validé par ING",
                currentUser.getEmail(),
                "ING"
        );

        return validated;
    }
    @Transactional
    public ChargeSheet validateByPt(Long sheetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que l'utilisateur est PT
        if (!currentUser.getRole().name().equals("PT") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT peut valider cette étape");
        }

        // Vérifier que tous les items sont TECH_FILLED
        boolean allItemsFilled = chargeSheet.getItems().stream()
                .allMatch(item -> "TECH_FILLED".equals(item.getItemStatus()));

        if (!allItemsFilled) {
            throw new RuntimeException("Tous les items doivent être remplis avant validation PT");
        }

        ChargeSheetStatus newStatus;
        String message;

        // WORKFLOW COMPLET:
        // DRAFT → VALIDATED_ING → TECH_FILLED → VALIDATED_PT → SENT_TO_SUPPLIER → RECEIVED_FROM_SUPPLIER → COMPLETED

        switch (chargeSheet.getStatus()) {
            case VALIDATED_ING:
                // ING a validé, PT valide maintenant → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT - En attente d'envoi au fournisseur";
                break;

            case TECH_FILLED:
                // Les items sont remplis, PT valide maintenant → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT - En attente d'envoi au fournisseur";
                break;

            case DRAFT:
                // Cas exceptionnel: PT valide directement un brouillon → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT directement - En attente d'envoi au fournisseur";
                break;

            default:
                throw new RuntimeException("Statut invalide pour validation PT: " + chargeSheet.getStatus());
        }

        chargeSheet.setStatus(newStatus);
        chargeSheet.setUpdatedBy(currentUser.getEmail());
        chargeSheet.setUpdatedAt(LocalDate.now());

        ChargeSheet validated = repository.save(chargeSheet);

        // Notification
        notificationService.notifyChargeSheetUpdated(
                sheetId,
                message,
                currentUser.getEmail(),
                "PT"
        );

        return validated;
    }
    public Map<String, Object> getDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProject = currentUser.getProjet();

        Map<String, Object> stats = new HashMap<>();

        // Informations générales
        stats.put("userRole", userRole);
        stats.put("userProject", userProject);
        stats.put("projectName", userProject);

        // Total des cahiers dans le projet
        long totalSheets = repository.countByProject(userProject);
        stats.put("totalSheets", totalSheets);

        // Cahiers en attente de validation ING (DRAFT)
        long pendingIng = repository.countByProjectAndStatus(userProject, ChargeSheetStatus.DRAFT);
        stats.put("pendingIng", pendingIng);

        // Cahiers en attente de validation PT (VALIDATED_ING)
        long pendingPt = repository.countByProjectAndStatus(userProject, ChargeSheetStatus.VALIDATED_ING);
        stats.put("pendingPt", pendingPt);

        // Cahiers validés PT en attente de validation finale (VALIDATED_PT)
        long pendingFinal = repository.countByProjectAndStatus(userProject, ChargeSheetStatus.VALIDATED_PT);
        stats.put("pendingFinal", pendingFinal);

        // Cahiers complétés
        long completed = repository.countByProjectAndStatus(userProject, ChargeSheetStatus.COMPLETED);
        stats.put("completed", completed);

        // Cahiers en cours de remplissage technique (TECH_FILLED)
        long techFilled = repository.countByProjectAndStatus(userProject, ChargeSheetStatus.TECH_FILLED);
        stats.put("techFilled", techFilled);

        // Pourcentage de complétion
        double completionRate = totalSheets > 0 ? (completed * 100.0 / totalSheets) : 0;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        return stats;
    }
    @Transactional
    public ChargeSheet sendToSupplier(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut envoyer au fournisseur");
        }

        if (sheet.getStatus() != ChargeSheetStatus.VALIDATED_PT) {
            throw new RuntimeException("Le cahier doit être VALIDATED_PT");
        }

        sheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    @Transactional
    public ChargeSheet confirmReception(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut confirmer la réception");
        }

        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être envoyé au fournisseur");
        }

        sheet.setStatus(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    @Transactional
    public ChargeSheet completeChargeSheet(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut compléter le cahier");
        }

        if (sheet.getStatus() != ChargeSheetStatus.RECEIVED_FROM_SUPPLIER) {
            throw new RuntimeException("La réception doit être confirmée avant completion");
        }

        sheet.setStatus(ChargeSheetStatus.COMPLETED);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    public ChargeSheet updateChargeSheet(Long id, ChargeSheetDto.UpdateGeneralDto dto) {
        ChargeSheet sheet = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que le statut est DRAFT
        if (sheet.getStatus() != ChargeSheetStatus.DRAFT) {
            throw new RuntimeException("Cannot update charge sheet when status is not DRAFT");
        }

        // Vérifier les permissions (l'utilisateur doit être ING du même projet)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getRole().name().equals("ING") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Only ING can update charge sheets");
        }

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !sheet.getProject().equals(currentUser.getProjet())) {
            throw new RuntimeException("You can only update charge sheets from your project");
        }

        // Mettre à jour les champs
        sheet.setPlant(dto.getPlant());
        sheet.setProject(dto.getProject());
        sheet.setHarnessRef(dto.getHarnessRef());
        sheet.setPhoneNumber(dto.getPhoneNumber());
        sheet.setOrderNumber(dto.getOrderNumber());
        sheet.setCostCenterNumber(dto.getCostCenterNumber());
        sheet.setDate(dto.getDate() != null ? LocalDate.parse(dto.getDate()) : null);
        sheet.setPreferredDeliveryDate(dto.getPreferredDeliveryDate() != null ?
                LocalDate.parse(dto.getPreferredDeliveryDate()) : null);

        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    // ============ MÉTHODES DE RÉCEPTION ============

    /**
     * Prépare les données pour la réception (quantités commandées, déjà reçues, restantes)
     */
    public ReceptionDto.ReceptionResponseDto prepareReceptionData(Long sheetId) {
        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier le statut
        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être en statut SENT_TO_SUPPLIER");
        }

        // Récupérer l'historique des réceptions pour calculer les quantités déjà reçues
        Map<Long, Integer> totalReceivedMap = new HashMap<>();
        List<ReceptionHistory> historyList = receptionHistoryRepository.findByChargeSheetId(sheetId);

        for (ReceptionHistory history : historyList) {
            totalReceivedMap.put(history.getItem().getId(),
                    totalReceivedMap.getOrDefault(history.getItem().getId(), 0) + history.getQuantityReceived());
        }

        // Préparer la liste des items avec les quantités
        List<ReceptionDto.ReceptionItemDto> itemDtos = new ArrayList<>();

        for (ChargeSheetItem item : sheet.getItems()) {
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityRemaining = quantityOrdered - totalReceived;

            ReceptionDto.ReceptionItemDto itemDto = ReceptionDto.ReceptionItemDto.builder()
                    .itemId(item.getId())
                    .itemNumber(item.getItemNumber())
                    .quantityOrdered(quantityOrdered)
                    .quantityReceived(0)
                    .quantityRemaining(quantityRemaining)
                    .build();

            itemDtos.add(itemDto);
        }

        boolean isComplete = itemDtos.stream().allMatch(item -> item.getQuantityRemaining() == 0);

        return ReceptionDto.ReceptionResponseDto.builder()
                .chargeSheetId(sheetId)
                .items(itemDtos)
                .message("Prêt pour la réception")
                .complete(isComplete)
                .build();
    }

    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    @Transactional
    public ReceptionDto.ReceptionResponseDto confirmPartialReception(ReceptionDto.ReceptionRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(request.getChargeSheetId())
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être en statut SENT_TO_SUPPLIER");
        }

        // Récupérer les totaux déjà reçus
        Map<Long, Integer> totalReceivedMap = new HashMap<>();
        List<ReceptionHistory> existingHistory = receptionHistoryRepository.findByChargeSheetId(sheet.getId());
        for (ReceptionHistory h : existingHistory) {
            totalReceivedMap.put(h.getItem().getId(),
                    totalReceivedMap.getOrDefault(h.getItem().getId(), 0) + h.getQuantityReceived());
        }

        // Traiter chaque item reçu
        for (ReceptionDto.ReceptionItemDto itemDto : request.getItems()) {
            if (itemDto.getQuantityReceived() <= 0) continue;

            ChargeSheetItem item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + itemDto.getItemId()));

            int previousTotal = totalReceivedMap.getOrDefault(item.getId(), 0);
            int newTotal = previousTotal + itemDto.getQuantityReceived();
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            // Vérifier qu'on ne dépasse pas la quantité commandée
            if (newTotal > quantityOrdered) {
                throw new RuntimeException("La quantité reçue pour l'item " + item.getItemNumber() +
                        " dépasse la quantité commandée (" + quantityOrdered + ")");
            }

            // Créer l'historique
            ReceptionHistory history = ReceptionHistory.builder()
                    .chargeSheet(sheet)
                    .item(item)
                    .quantityReceived(itemDto.getQuantityReceived())
                    .previousTotalReceived(previousTotal)
                    .newTotalReceived(newTotal)
                    .quantityOrdered(quantityOrdered)
                    .deliveryNoteNumber(request.getDeliveryNoteNumber())
                    .receptionDate(request.getReceptionDate() != null ?
                            LocalDate.parse(request.getReceptionDate()) : LocalDate.now())
                    .receivedBy(currentUser.getEmail())
                    .comments(request.getComments())
                    .createdAt(LocalDate.now())
                    .build();

            receptionHistoryRepository.save(history);

            // Mettre à jour la map des totaux reçus
            totalReceivedMap.put(item.getId(), newTotal);
        }

        // 🔥 VÉRIFIER SI TOUS LES ITEMS SONT COMPLÈTEMENT REÇUS
        boolean allItemsCompletelyReceived = true;
        StringBuilder remainingItemsMessage = new StringBuilder();

        for (ChargeSheetItem item : sheet.getItems()) {
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            if (totalReceived < quantityOrdered) {
                allItemsCompletelyReceived = false;
                remainingItemsMessage.append("Item ").append(item.getItemNumber())
                        .append(": ").append(totalReceived).append("/").append(quantityOrdered).append(" reçus, ")
                        .append(quantityOrdered - totalReceived).append(" restant\n");
            }
        }

        // ⚠️ NE PAS CHANGER LE STATUT SI TOUS LES ITEMS NE SONT PAS ENCORE REÇUS
        // Le statut reste SENT_TO_SUPPLIER tant qu'il reste des items à recevoir
        if (allItemsCompletelyReceived) {
            sheet.setStatus(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
            sheet.setUpdatedBy(currentUser.getEmail());
            sheet.setUpdatedAt(LocalDate.now());
            repository.save(sheet);
        }

        // Recalculer les quantités restantes pour la réponse
        List<ReceptionDto.ReceptionItemDto> responseItems = new ArrayList<>();
        for (ChargeSheetItem item : sheet.getItems()) {
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            responseItems.add(ReceptionDto.ReceptionItemDto.builder()
                    .itemId(item.getId())
                    .itemNumber(item.getItemNumber())
                    .quantityOrdered(quantityOrdered)
                    .quantityReceived(0)
                    .quantityRemaining(quantityOrdered - totalReceived)
                    .build());
        }

        String message;
        if (allItemsCompletelyReceived) {
            message = "✅ Tous les items ont été reçus complètement. Le cahier passe en statut RECEIVED_FROM_SUPPLIER";
        } else {
            message = "📦 Réception partielle enregistrée avec succès.\n" +
                    "Items restants à recevoir :\n" + remainingItemsMessage.toString();
        }

        return ReceptionDto.ReceptionResponseDto.builder()
                .chargeSheetId(sheet.getId())
                .items(responseItems)
                .message(message)
                .complete(allItemsCompletelyReceived)
                .build();
    }

    /**
     * Récupère l'historique des réceptions
     */
    public List<ReceptionHistoryDto> getReceptionHistoryDto(Long sheetId) {
        List<ReceptionHistory> histories = receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(sheetId);

        return histories.stream().map(history -> {
            ReceptionHistoryDto.ItemInfoDto itemInfo = ReceptionHistoryDto.ItemInfoDto.builder()
                    .id(history.getItem().getId())
                    .itemNumber(history.getItem().getItemNumber())
                    .build();

            return ReceptionHistoryDto.builder()
                    .id(history.getId())
                    .item(itemInfo)
                    .quantityReceived(history.getQuantityReceived())
                    .previousTotalReceived(history.getPreviousTotalReceived())
                    .newTotalReceived(history.getNewTotalReceived())
                    .quantityOrdered(history.getQuantityOrdered())
                    .deliveryNoteNumber(history.getDeliveryNoteNumber())
                    .receptionDate(history.getReceptionDate())
                    .receivedBy(history.getReceivedBy())
                    .comments(history.getComments())
                    .createdAt(history.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
// Dans ChargeSheetService.java - Modifiez les méthodes pour l'admin

    /**
     * Calcule la variation entre deux mois spécifiques
     */
    public Map<String, Object> getVariationBetweenMonths(String project, String month1, String month2) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            // Utilisateur normal ou admin avec projet spécifique
            String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();
            results = repository.countByMonthForProject(userProject);
        }

        Map<String, Long> monthlyCounts = new HashMap<>();
        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        Long countMonth1 = monthlyCounts.getOrDefault(month1, 0L);
        Long countMonth2 = monthlyCounts.getOrDefault(month2, 0L);

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countMonth1 > 0) {
            variation = ((countMonth2 - countMonth1) * 100.0) / countMonth1;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countMonth2, countMonth1, countMonth1, variation);
        } else if (countMonth1 == 0 && countMonth2 > 0) {
            variation = 100.0;
            trend = "hausse";
            formula = String.format("((%d - %d) / %d) × 100 = %.1f%% (création depuis zéro)",
                    countMonth2, countMonth1, 1, variation);
        } else if (countMonth1 == 0 && countMonth2 == 0) {
            formula = "Aucune création sur les deux mois";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("project", isAdmin && (project == null || project.isEmpty()) ? "TOUS_PROJETS" : project);
        result.put("month1", month1);
        result.put("month1Count", countMonth1);
        result.put("month2", month2);
        result.put("month2Count", countMonth2);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);

        return result;
    }

    /**
     * Récupère les statistiques de création mensuelles
     */
    public MonthlyStatsDto getMonthlyCreationStats(String project, int numberOfMonths) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            // Utilisateur normal ou admin avec projet spécifique
            String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();
            results = repository.countByMonthForProject(userProject);
        }

        Map<String, Long> monthlyCounts = new LinkedHashMap<>();
        Map<String, Double> monthlyVariations = new LinkedHashMap<>();

        // Transformer les résultats en Map
        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        // Calculer les variations
        List<String> months = new ArrayList<>(monthlyCounts.keySet());
        for (int i = 0; i < months.size() - 1; i++) {
            String currentMonth = months.get(i);
            String previousMonth = months.get(i + 1);

            Long currentCount = monthlyCounts.get(currentMonth);
            Long previousCount = monthlyCounts.get(previousMonth);

            if (previousCount != null && previousCount > 0) {
                double variation = ((currentCount - previousCount) * 100.0) / previousCount;
                monthlyVariations.put(currentMonth, Math.round(variation * 10.0) / 10.0);
            } else if (previousCount != null && previousCount == 0) {
                monthlyVariations.put(currentMonth, currentCount > 0 ? 100.0 : 0.0);
            } else {
                monthlyVariations.put(currentMonth, 0.0);
            }
        }

        // Calculer la variation pour les deux derniers mois
        String currentMonth = null;
        String previousMonth = null;
        double variationPercentage = 0.0;
        String trend = "stable";

        if (months.size() >= 2) {
            currentMonth = months.get(0);
            previousMonth = months.get(1);
            variationPercentage = monthlyVariations.getOrDefault(currentMonth, 0.0);

            if (variationPercentage > 0) {
                trend = "hausse";
            } else if (variationPercentage < 0) {
                trend = "baisse";
            }
        }

        // Limiter au nombre de mois demandé
        Map<String, Long> limitedMonthlyCounts = new LinkedHashMap<>();
        Map<String, Double> limitedMonthlyVariations = new LinkedHashMap<>();

        int count = 0;
        for (Map.Entry<String, Long> entry : monthlyCounts.entrySet()) {
            if (count >= numberOfMonths) break;
            limitedMonthlyCounts.put(entry.getKey(), entry.getValue());
            if (monthlyVariations.containsKey(entry.getKey())) {
                limitedMonthlyVariations.put(entry.getKey(), monthlyVariations.get(entry.getKey()));
            }
            count++;
        }

        String formula = "";
        if (currentMonth != null && previousMonth != null) {
            Long currentCount = monthlyCounts.get(currentMonth);
            Long prevCount = monthlyCounts.get(previousMonth);
            if (prevCount != null && prevCount > 0) {
                formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                        currentCount, prevCount, prevCount, variationPercentage);
            }
        }

        return MonthlyStatsDto.builder()
                .monthlyCounts(limitedMonthlyCounts)
                .monthlyVariations(limitedMonthlyVariations)
                .currentMonth(currentMonth)
                .previousMonth(previousMonth)
                .variationPercentage(variationPercentage)
                .trend(trend)
                .formula(formula)
                .build();
    }

    /**
     * Version simplifiée pour le dashboard avec les deux derniers mois
     */
    public Map<String, Object> getLastTwoMonthsVariation(String project) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            // Utilisateur normal ou admin avec projet spécifique
            String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();
            results = repository.countByMonthForProject(userProject);
        }

        if (results == null || results.size() < 2) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("message", "Pas assez de données pour calculer la variation");
            emptyResult.put("availableMonths", results != null ? results.size() : 0);
            return emptyResult;
        }

        // Les résultats sont déjà triés par date DESC (du plus récent au plus ancien)
        Object[] latestMonth = results.get(0);     // Mois le plus récent (ex: Mars)
        Object[] previousMonth = results.get(1);   // Mois précédent (ex: Février)

        String monthLatest = (String) latestMonth[0];
        Long countLatest = ((Number) latestMonth[1]).longValue();
        String monthPrevious = (String) previousMonth[0];
        Long countPrevious = ((Number) previousMonth[1]).longValue();

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countPrevious > 0) {
            variation = ((countLatest - countPrevious) * 100.0) / countPrevious;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countLatest, countPrevious, countPrevious, variation);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentMonth", monthLatest);
        result.put("currentMonthCount", countLatest);
        result.put("previousMonth", monthPrevious);
        result.put("previousMonthCount", countPrevious);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);
        result.put("interpretation", getInterpretation(trend, variation, countLatest, countPrevious));
        result.put("isGlobalView", isAdmin && (project == null || project.isEmpty()));

        return result;
    }

    private String getInterpretation(String trend, double variation, long currentCount, long previousCount) {
        if (trend.equals("hausse")) {
            if (variation > 50) {
                return String.format("📈 Forte augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            } else {
                return String.format("📈 Augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            }
        } else if (trend.equals("baisse")) {
            if (variation < -50) {
                return String.format("📉 Forte baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            } else {
                return String.format("📉 Baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            }
        } else {
            return String.format("➡️ Stable (%d créations pour les deux mois)", currentCount);
        }
    }
}