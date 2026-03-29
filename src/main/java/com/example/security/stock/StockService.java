package com.example.security.stock;

import com.example.security.fichierTechnique.TechnicalFile;
import com.example.security.fichierTechnique.TechnicalFileItem;
import com.example.security.fichierTechnique.TechnicalFileService;
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
public class StockService {

    private final StockModuleRepository stockRepository;
    private final TechnicalFileService technicalFileService;

    public StockService(StockModuleRepository stockRepository,
                        TechnicalFileService technicalFileService) {
        this.stockRepository = stockRepository;
        this.technicalFileService = technicalFileService;
    }

    @Transactional
    public StockModule moveToStock(Long technicalFileId) {
        // Récupérer le dossier technique avec ses items
        TechnicalFile technicalFile = technicalFileService.getTechnicalFileById(technicalFileId);

        // Vérifier qu'il y a au moins un item
        if (technicalFile.getTechnicalFileItems() == null || technicalFile.getTechnicalFileItems().isEmpty()) {
            throw new RuntimeException("Le dossier technique ne contient aucun item");
        }

        // Auth utilisateur courant
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Pour chaque item, on pourrait créer un module en stock
        // Mais ici on va créer un module par item du dossier

        List<StockModule> stockModules = new ArrayList<>();

        for (TechnicalFileItem item : technicalFile.getTechnicalFileItems()) {
            // Extraire les valeurs finales pour cet item
            Double finalDisplacement = pickFinalValue(
                    item.getDisplacementPathM1(),
                    item.getDisplacementPathM2(),
                    item.getDisplacementPathM3()
            );

            Double finalProgrammedSealing = pickFinalValue(
                    item.getProgrammedSealingValueM1(),
                    item.getProgrammedSealingValueM2(),
                    item.getProgrammedSealingValueM3()
            );

            String finalDetection = pickFinalDetection(
                    item.getDetectionsM1(),
                    item.getDetectionsM2(),
                    item.getDetectionsM3()
            );

            // Créer et sauvegarder le module en stock
            StockModule stock = StockModule.builder()
                    .technicalFileItem(item)  // Lier à l'item spécifique
                    .technicalFile(technicalFile)  // Garder aussi la référence au dossier
                    .chargeSheetItemId(item.getChargeSheetItem().getId())
                    .itemNumber(item.getChargeSheetItem().getItemNumber())
                    .position(item.getPosition())
                    .finalDisplacement(finalDisplacement)
                    .finalProgrammedSealing(finalProgrammedSealing)
                    .finalDetection(finalDetection)
                    .movedBy(currentUser.getEmail())
                    .movedAt(LocalDate.now())
                    .status(StockModule.StockStatus.AVAILABLE)
                    .build();

            stockModules.add(stockRepository.save(stock));
        }

        // Optionnel : retourner le premier module ou la liste
        return stockModules.isEmpty() ? null : stockModules.get(0);
    }
    // Dans StockService.java
    // Dans StockService.java
    @Transactional
    public StockModule moveItemToStock(Long technicalFileItemId) {
        // Vérifier si l'item est déjà en stock
        StockModule existingStock = stockRepository.findByTechnicalFileItemId(technicalFileItemId).orElse(null);

        if (existingStock != null) {
            throw new RuntimeException("Cet item est déjà présent dans le stock. Veuillez utiliser la mise à jour de statut.");
        }

        // Récupérer l'item technique
        TechnicalFileItem item = technicalFileService.getTechnicalFileItemById(technicalFileItemId);

        // Auth utilisateur courant
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Extraire les valeurs finales pour cet item
        Double finalDisplacement = pickFinalValue(
                item.getDisplacementPathM1(),
                item.getDisplacementPathM2(),
                item.getDisplacementPathM3()
        );

        Double finalProgrammedSealing = pickFinalValue(
                item.getProgrammedSealingValueM1(),
                item.getProgrammedSealingValueM2(),
                item.getProgrammedSealingValueM3()
        );

        String finalDetection = pickFinalDetection(
                item.getDetectionsM1(),
                item.getDetectionsM2(),
                item.getDetectionsM3()
        );

        // Créer et sauvegarder le module en stock
        StockModule stock = StockModule.builder()
                .technicalFileItem(item)
                .technicalFile(item.getTechnicalFile())
                .chargeSheetItemId(item.getChargeSheetItem().getId())
                .itemNumber(item.getChargeSheetItem().getItemNumber())
                .position(item.getPosition())
                .finalDisplacement(finalDisplacement)
                .finalProgrammedSealing(finalProgrammedSealing)
                .finalDetection(finalDetection)
                .movedBy(currentUser.getEmail())
                .movedAt(LocalDate.now())
                .status(StockModule.StockStatus.AVAILABLE)
                .build();

        return stockRepository.save(stock);
    }
    // Lister tous les modules
    public List<StockModule> getAllStock() {
        return stockRepository.findAll();
    }

    // Récupérer un module par ID
    public StockModule getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module stock non trouvé"));
    }

    // --- Fonctions utilitaires ---
    private Double pickFinalValue(String m1, String m2, String m3){
        Double val1 = parseDoubleSafe(m1);
        Double val2 = parseDoubleSafe(m2);
        Double val3 = parseDoubleSafe(m3);

        // Utiliser ArrayList au lieu de List.of() qui n'accepte pas les nulls
        List<Double> values = new ArrayList<>();
        if (val1 != null) values.add(val1);
        if (val2 != null) values.add(val2);
        if (val3 != null) values.add(val3);

        return values.stream().max(Double::compare).orElse(null);
    }

    private String pickFinalDetection(String m1, String m2, String m3){
        // Par exemple, priorité à M3 > M2 > M1 si activé
        if(m3 != null && !m3.isEmpty()) return m3;
        if(m2 != null && !m2.isEmpty()) return m2;
        return m1;
    }

    private Double parseDoubleSafe(String val){
        try {
            return val != null ? Double.parseDouble(val) : null;
        } catch(Exception e){
            return null;
        }
    }
    @Transactional
    public StockModule changeStatus(Long id, StockModule.StockStatus status) {
        StockModule module = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module stock non trouvé"));
        module.setStatus(status);
        return stockRepository.save(module);
    }


}
