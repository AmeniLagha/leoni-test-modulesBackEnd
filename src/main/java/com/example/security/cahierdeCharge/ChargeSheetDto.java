package com.example.security.cahierdeCharge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChargeSheetDto {
// Ajouter cette classe dans ChargeSheetDto.java

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

        @Builder.Default
        private List<ItemDto> items = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTechDto {
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