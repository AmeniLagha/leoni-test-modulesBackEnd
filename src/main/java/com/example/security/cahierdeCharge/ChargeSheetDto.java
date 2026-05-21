package com.example.security.cahierdeCharge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Objet de transfert de données (DTO) pour l'entité {@link ChargeSheet}.
 * <p>
 * Cette classe contient plusieurs DTOs internes pour les différentes
 * opérations sur les cahiers des charges : création, mise à jour,
 * consultation et gestion des items techniques.
 * </p>
 *
 * <p><strong>DTOs disponibles :</strong></p>
 * <ul>
 *     <li>{@link UpdateGeneralDto} : Mise à jour des informations générales (DRAFT)</li>
 *     <li>{@link ItemDto} : Représentation d'un item technique (connecteur)</li>
 *     <li>{@link CreateDto} : Création d'un nouveau cahier avec ses items</li>
 *     <li>{@link UpdateTechDto} : Mise à jour des 150+ champs techniques d'un item</li>
 *     <li>{@link CompleteDto} : Vue complète d'un cahier avec tous ses items</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheet
 * @see ChargeSheetItem
 * @since Sprint 4
 */
public class ChargeSheetDto {
    // ============================================================
    // DTO - MISE À JOUR DES INFORMATIONS GÉNÉRALES
    // ============================================================

    /**
     * DTO pour la mise à jour des informations générales d'un cahier des charges.
     * <p>
     * Utilisé lorsque l'ingénieur modifie un cahier en statut {@code DRAFT}.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateGeneralDto {
        private String plant;
        private String project;
        private String harnessRef;
        private String phoneNumber;
        private String orderNumber;
        private String costCenterNumber;
        private String date;
        private String preferredDeliveryDate;

    }
    // ============================================================
    // DTO - ITEM TECHNIQUE (CONNECTEUR)
    // ============================================================

    /**
     * DTO représentant un item technique (connecteur) d'un cahier des charges.
     * <p>
     * Contient l'ensemble des champs nécessaires pour la gestion des items :
     * informations générales, critères de test, résultats techniques, prix, etc.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private Long id;
        private String itemNumber;
        private String samplesExist;
        private String ways;
        private String housingColour;
        private String testModuleExistInDatabase;
        private String housingReferenceLeoni;
        private String housingReferenceSupplierCustomer;
        private String referenceSealsClipsCableTiesCap;
        private String realConnectorPicture;
        private Integer quantityOfTestModules;

        // STANDARD TEST CRITERIA
        private String outsideHousingExist;
        private String insideHousingExist;
        private String mechanicalCoding;
        private String electricalCoding;
        private String cpaExistOpen;
        private String cpaExistClosed;
        private String coverHoodExist;
        private String coverHoodClosed;
        private String capExist;
        private String bayonetCapExist;
        private String bracketExist;
        private String bracketOpen;
        private String bracketClosed;
        private String latchWingExist;
        private String sliderExist;
        private String sliderOpen;
        private String sliderClosed;
        private String secondaryLockExist;
        private String secondaryLockOpen;
        private String secondaryLockClosed;
        private String offsetTest;
        private String pushBackTest;
        private String terminalOrientation;
        private String terminalDifferentiation;
        private String airbagTestViaServiceWindow;
        private String leakTestPressure;
        private String leakTestVacuum;
        private String sealExist;
        private String cableTieExist;
        private String cableTieLeft;
        private String cableTieRight;
        private String cableTieMiddle;
        private String cableTieLeftRight;
        private String clipExist;
        private String screwExist;
        private String nutExist;
        private String convolutedConduitExist;
        private String convolutedConduitClosed;
        private String antennaOnlyPresenceTest;
        private String antennaOnlyContactingOfShield;
        private String antennaContactingOfShieldAndCoreWire;
        private String ringTerminal;
        private String diameterInside;
        private String diameterOutside;
        private String singleContact;
        private String heatShrinkExist;
        private String openShuntsAirbag;
        private String flowTest;
        private String solidMetalContour;
        private String metalContourAdjustable;
        private String grommetExist;
        private String grommetOrientation;
        private String cableChannelExist;
        private String cableChannelClosed;
        private String colourDetectionPrepared;
        private String extraLED;
        private String spring;
        private String otherDetection;
        private String spacerClosingUnit;
        private String leakTestComplex;
        private String pinStraightnessCheck;
        private String presenceTestOfOneSideConnectedShield;
        private String contrastDetectionGreyValueSensor;
        private String colourDetection;
        private String attenuationWithModeScrambler;
        private String attenuationWithoutModeScrambler;
        private String insulationResistance;
        private String highVoltageModule;
        private String kelvinMeasurementHV;
        private String actuatorTestHV;
        private String chargingSystemElectrical;
        private String ptuPipeTestUnit;
        private String gtuGrommetTestUnit;
        private String ledLEDTestModule;
        private String tigTerminalInsertionGuidance;
        private String linBusFunctionalityTest;
        private String canBusFunctionalityTest;
        private String esdConformModule;
        private String fixedBlock;
        private String movingBlock;
        private String tiltModule;
        private String slideModule;
        private String handAdapter;
        private String lsmLeoniSmartModule;
        private String leoniStandardTestTable;
        private String metalRailsFasteningSystem;
        private String metalPlatesFasteningSystem;
        private String quickConnectionByCanonConnector;
        private String testBoard;
        private String weetech;
        private String bak;
        private String ogc;
        private String adaptronicHighVoltage;
        private String emdepHVBananaPlug;
        private String leoniEMOStandardHV;
        private String clipOrientation;
        private Double unitPrice;
        private Double totalPrice;
        private String itemStatus;
        private String createdBy;
        private LocalDate createdAt;
        private String updatedBy;
        private LocalDate updatedAt;
    }
    // ============================================================
    // DTO - CRÉATION D'UN CAHIER
    // ============================================================

    /**
     * DTO pour la création d'un nouveau cahier des charges.
     * <p>
     * Utilisé lors de la création initiale par l'ingénieur.
     * Contient les informations générales et la liste des items associés.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {
        private String plant;
        private String project;
        private String harnessRef;
        private String issuedBy;
        private String emailAddress;
        private String phoneNumber;
        private String orderNumber;
        private String costCenterNumber;
        private LocalDate date;
        private LocalDate preferredDeliveryDate;
        /** Liste des items (connecteurs) à créer. */
        @Builder.Default
        private List<ItemDto> items = new ArrayList<>();
    }
    // ============================================================
    // DTO - MISE À JOUR DES CHAMPS TECHNIQUES
    // ============================================================

    /**
     * DTO pour la mise à jour des champs techniques d'un item.
     * <p>
     * Utilisé par le technicien (PT) pour saisir les 150+ attributs
     * techniques d'un connecteur via l'interface de type tableur.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTechDto {
        private String housingReferenceLeoni;
        private Integer quantityOfTestModules;
        private String outsideHousingExist;
        private String insideHousingExist;
        private String mechanicalCoding;
        private String electricalCoding;
        private String cpaExistOpen;
        private String cpaExistClosed;
        private String coverHoodExist;
        private String coverHoodClosed;
        private String capExist;
        private String bayonetCapExist;
        private String bracketExist;
        private String bracketOpen;
        private String bracketClosed;
        private String latchWingExist;
        private String sliderExist;
        private String sliderOpen;
        private String sliderClosed;
        private String secondaryLockExist;
        private String secondaryLockOpen;
        private String secondaryLockClosed;
        private String offsetTest;
        private String pushBackTest;
        private String terminalOrientation;
        private String terminalDifferentiation;
        private String airbagTestViaServiceWindow;
        private String leakTestPressure;
        private String leakTestVacuum;
        private String sealExist;
        private String cableTieExist;
        private String cableTieLeft;
        private String cableTieRight;
        private String cableTieMiddle;
        private String cableTieLeftRight;
        private String clipExist;
        private String screwExist;
        private String nutExist;
        private String convolutedConduitExist;
        private String convolutedConduitClosed;
        private String antennaOnlyPresenceTest;
        private String antennaOnlyContactingOfShield;
        private String antennaContactingOfShieldAndCoreWire;
        private String ringTerminal;
        private String diameterInside;
        private String diameterOutside;
        private String singleContact;
        private String heatShrinkExist;
        private String openShuntsAirbag;
        private String flowTest;
        private String solidMetalContour;
        private String metalContourAdjustable;
        private String grommetExist;
        private String grommetOrientation;
        private String cableChannelExist;
        private String cableChannelClosed;
        private String colourDetectionPrepared;
        private String extraLED;
        private String spring;
        private String otherDetection;
        private String spacerClosingUnit;
        private String leakTestComplex;
        private String pinStraightnessCheck;
        private String presenceTestOfOneSideConnectedShield;
        private String contrastDetectionGreyValueSensor;
        private String colourDetection;
        private String attenuationWithModeScrambler;
        private String attenuationWithoutModeScrambler;
        private String insulationResistance;
        private String highVoltageModule;
        private String kelvinMeasurementHV;
        private String actuatorTestHV;
        private String chargingSystemElectrical;
        private String ptuPipeTestUnit;
        private String gtuGrommetTestUnit;
        private String ledLEDTestModule;
        private String tigTerminalInsertionGuidance;
        private String linBusFunctionalityTest;
        private String canBusFunctionalityTest;
        private String esdConformModule;
        private String fixedBlock;
        private String movingBlock;
        private String tiltModule;
        private String slideModule;
        private String handAdapter;
        private String lsmLeoniSmartModule;
        private String leoniStandardTestTable;
        private String metalRailsFasteningSystem;
        private String metalPlatesFasteningSystem;
        private String quickConnectionByCanonConnector;
        private String testBoard;
        private String weetech;
        private String bak;
        private String ogc;
        private String adaptronicHighVoltage;
        private String emdepHVBananaPlug;
        private String leoniEMOStandardHV;
        private String clipOrientation;
        private Double unitPrice;
        private Double totalPrice;
    }
    // ============================================================
    // DTO - VUE COMPLÈTE D'UN CAHIER
    // ============================================================

    /**
     * DTO pour la vue complète d'un cahier des charges.
     * <p>
     * Contient toutes les informations générales du cahier ainsi que
     * la liste complète de ses items avec leurs attributs techniques.
     * Utilisé pour les réponses API de consultation.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteDto {
        private Long id;
        private String plant;
        private String project;
        private String harnessRef;
        private String issuedBy;
        private String emailAddress;
        private String phoneNumber;
        private String orderNumber;
        private String costCenterNumber;
        private LocalDate date;
        private LocalDate preferredDeliveryDate;
        private List<ItemDto> items;
        private ChargeSheetStatus status;
        private String createdBy;
        private LocalDate createdAt;
        private String updatedBy;
        private LocalDate updatedAt;
    }
}