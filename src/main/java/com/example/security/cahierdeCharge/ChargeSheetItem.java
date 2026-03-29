package com.example.security.cahierdeCharge;

import com.example.security.fichierTechnique.TechnicalFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "charge_sheet_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeSheetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_sheet_id", nullable = false)
    private ChargeSheet chargeSheet;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_file_id", nullable = true)
    private TechnicalFile technicalFile;

    // === ITEM IDENTIFICATION ===
    @Column(name = "item_number", length = 10)
    private String itemNumber;

    @Column(name = "samples_exist", length = 3)
    private String samplesExist;

    @Column(name = "ways", length = 5)
    private String ways;

    @Column(name = "housing_colour", length = 30)
    private String housingColour;

    @Column(name = "test_module_exist_in_database", length = 3)
    private String testModuleExistInDatabase;

    @Column(name = "housing_reference_leoni", length = 50)
    private String housingReferenceLeoni;

    @Column(name = "housing_reference_supplier_customer", length = 100)
    private String housingReferenceSupplierCustomer;

    @Column(name = "reference_seals_clips_cable_ties_cap", length = 100)
    private String referenceSealsClipsCableTiesCap;

    @Column(name = "real_connector_picture", length = 255)
    private String realConnectorPicture;

    @Column(name = "quantity_of_test_modules")
    private Integer quantityOfTestModules;

    // === STANDARD TEST CRITERIA ===
    @Column(name = "outside_housing_exist", length = 1)
    private String outsideHousingExist;

    @Column(name = "inside_housing_exist", length = 1)
    private String insideHousingExist;

    @Column(name = "mechanical_coding", length = 1)
    private String mechanicalCoding;

    @Column(name = "electrical_coding", length = 1)
    private String electricalCoding;

    @Column(name = "cpa_exist_open", length = 1)
    private String cpaExistOpen;

    @Column(name = "cpa_exist_closed", length = 1)
    private String cpaExistClosed;

    @Column(name = "cover_hood_exist", length = 1)
    private String coverHoodExist;

    @Column(name = "cover_hood_closed", length = 1)
    private String coverHoodClosed;

    @Column(name = "cap_exist", length = 1)
    private String capExist;

    @Column(name = "bayonet_cap_exist", length = 1)
    private String bayonetCapExist;

    @Column(name = "bracket_exist", length = 1)
    private String bracketExist;

    @Column(name = "bracket_open", length = 1)
    private String bracketOpen;

    @Column(name = "bracket_closed", length = 1)
    private String bracketClosed;

    @Column(name = "latch_wing_exist", length = 1)
    private String latchWingExist;

    @Column(name = "slider_exist", length = 1)
    private String sliderExist;

    @Column(name = "slider_open", length = 1)
    private String sliderOpen;

    @Column(name = "slider_closed", length = 1)
    private String sliderClosed;

    @Column(name = "secondary_lock_exist", length = 1)
    private String secondaryLockExist;

    @Column(name = "secondary_lock_open", length = 1)
    private String secondaryLockOpen;

    @Column(name = "secondary_lock_closed", length = 1)
    private String secondaryLockClosed;

    @Column(name = "offset_test", length = 1)
    private String offsetTest;

    @Column(name = "push_back_test", length = 1)
    private String pushBackTest;

    @Column(name = "terminal_orientation", length = 1)
    private String terminalOrientation;

    @Column(name = "terminal_differentiation", length = 1)
    private String terminalDifferentiation;

    @Column(name = "airbag_test_via_service_window", length = 1)
    private String airbagTestViaServiceWindow;

    @Column(name = "leak_test_pressure", length = 1)
    private String leakTestPressure;

    @Column(name = "leak_test_vacuum", length = 1)
    private String leakTestVacuum;

    @Column(name = "seal_exist", length = 1)
    private String sealExist;

    @Column(name = "cable_tie_exist", length = 1)
    private String cableTieExist;

    @Column(name = "cable_tie_left", length = 1)
    private String cableTieLeft;

    @Column(name = "cable_tie_right", length = 1)
    private String cableTieRight;

    @Column(name = "cable_tie_middle", length = 1)
    private String cableTieMiddle;

    @Column(name = "cable_tie_left_right", length = 1)
    private String cableTieLeftRight;

    @Column(name = "clip_exist", length = 1)
    private String clipExist;

    @Column(name = "screw_exist", length = 1)
    private String screwExist;

    @Column(name = "nut_exist", length = 1)
    private String nutExist;

    @Column(name = "convoluted_conduit_exist", length = 1)
    private String convolutedConduitExist;

    @Column(name = "convoluted_conduit_closed", length = 1)
    private String convolutedConduitClosed;

    @Column(name = "antenna_only_presence_test", length = 1)
    private String antennaOnlyPresenceTest;

    @Column(name = "antenna_only_contacting_of_shield", length = 1)
    private String antennaOnlyContactingOfShield;

    @Column(name = "antenna_contacting_of_shield_and_core_wire", length = 1)
    private String antennaContactingOfShieldAndCoreWire;

    @Column(name = "ring_terminal", length = 1)
    private String ringTerminal;

    @Column(name = "diameter_inside", length = 20)
    private String diameterInside;

    @Column(name = "diameter_outside", length = 20)
    private String diameterOutside;

    @Column(name = "single_contact", length = 1)
    private String singleContact;

    @Column(name = "heat_shrink_exist", length = 1)
    private String heatShrinkExist;

    @Column(name = "open_shunts_airbag", length = 1)
    private String openShuntsAirbag;

    @Column(name = "flow_test", length = 1)
    private String flowTest;

    @Column(name = "solid_metal_contour", length = 1)
    private String solidMetalContour;

    @Column(name = "metal_contour_adjustable", length = 1)
    private String metalContourAdjustable;

    @Column(name = "grommet_exist", length = 1)
    private String grommetExist;

    @Column(name = "grommet_orientation", length = 20)
    private String grommetOrientation;

    @Column(name = "cable_channel_exist", length = 1)
    private String cableChannelExist;

    @Column(name = "cable_channel_closed", length = 1)
    private String cableChannelClosed;

    @Column(name = "colour_detection_prepared", length = 1)
    private String colourDetectionPrepared;

    @Column(name = "extra_led", length = 1)
    private String extraLED;

    @Column(name = "spring", length = 1)
    private String spring;

    @Column(name = "other_detection", length = 1)
    private String otherDetection;

    @Column(name = "spacer_closing_unit", length = 1)
    private String spacerClosingUnit;

    @Column(name = "leak_test_complex", length = 1)
    private String leakTestComplex;

    @Column(name = "pin_straightness_check", length = 1)
    private String pinStraightnessCheck;

    @Column(name = "presence_test_of_one_side_connected_shield", length = 1)
    private String presenceTestOfOneSideConnectedShield;

    @Column(name = "contrast_detection_grey_value_sensor", length = 1)
    private String contrastDetectionGreyValueSensor;

    @Column(name = "colour_detection", length = 1)
    private String colourDetection;

    @Column(name = "attenuation_with_mode_scrambler", length = 1)
    private String attenuationWithModeScrambler;

    @Column(name = "attenuation_without_mode_scrambler", length = 1)
    private String attenuationWithoutModeScrambler;

    @Column(name = "insulation_resistance", length = 1)
    private String insulationResistance;

    @Column(name = "high_voltage_module", length = 1)
    private String highVoltageModule;

    @Column(name = "kelvin_measurement_hv", length = 1)
    private String kelvinMeasurementHV;

    @Column(name = "actuator_test_hv", length = 1)
    private String actuatorTestHV;

    @Column(name = "charging_system_electrical", length = 1)
    private String chargingSystemElectrical;

    // TYPE OF TEST MODULE
    @Column(name = "ptu_pipe_test_unit", length = 1)
    private String ptuPipeTestUnit;

    @Column(name = "gtu_grommet_test_unit", length = 1)
    private String gtuGrommetTestUnit;

    @Column(name = "led_led_test_module", length = 1)
    private String ledLEDTestModule;

    @Column(name = "tig_terminal_insertion_guidance", length = 1)
    private String tigTerminalInsertionGuidance;

    @Column(name = "lin_bus_functionality_test", length = 1)
    private String linBusFunctionalityTest;

    @Column(name = "can_bus_functionality_test", length = 1)
    private String canBusFunctionalityTest;

    @Column(name = "esd_conform_module", length = 1)
    private String esdConformModule;

    @Column(name = "fixed_block", length = 1)
    private String fixedBlock;

    @Column(name = "moving_block", length = 1)
    private String movingBlock;

    @Column(name = "tilt_module", length = 1)
    private String tiltModule;

    @Column(name = "slide_module", length = 1)
    private String slideModule;

    @Column(name = "hand_adapter", length = 1)
    private String handAdapter;

    @Column(name = "lsm_leoni_smart_module", length = 1)
    private String lsmLeoniSmartModule;

    // TEST SYSTEM
    @Column(name = "leoni_standard_test_table", length = 1)
    private String leoniStandardTestTable;

    @Column(name = "metal_rails_fastening_system", length = 1)
    private String metalRailsFasteningSystem;

    @Column(name = "metal_plates_fastening_system", length = 1)
    private String metalPlatesFasteningSystem;

    @Column(name = "quick_connection_by_canon_connector", length = 1)
    private String quickConnectionByCanonConnector;

    @Column(name = "test_board", length = 50)
    private String testBoard;

    @Column(name = "weetech", length = 50)
    private String weetech;

    @Column(name = "bak", length = 50)
    private String bak;

    @Column(name = "ogc", length = 50)
    private String ogc;

    @Column(name = "adaptronic_high_voltage", length = 50)
    private String adaptronicHighVoltage;

    @Column(name = "emdep_hv_banana_plug", length = 50)
    private String emdepHVBananaPlug;

    @Column(name = "leoni_emo_standard_hv", length = 50)
    private String leoniEMOStandardHV;

    @Column(name = "clip_orientation", length = 50)
    private String clipOrientation;

    // PRICE
    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    // METADATA
    @Column(name = "item_status", length = 20)
    private String itemStatus;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @OneToMany(mappedBy = "item")
    @JsonIgnore
    private List<ReceptionHistory> receptionHistories;
}