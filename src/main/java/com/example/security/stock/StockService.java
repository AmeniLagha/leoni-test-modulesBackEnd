package com.example.security.stock;

import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.fichierTechnique.TechnicalFile;
import com.example.security.fichierTechnique.TechnicalFileItem;
import com.example.security.fichierTechnique.TechnicalFileItemStatus;
import com.example.security.fichierTechnique.TechnicalFileService;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockModuleRepository stockRepository;
    private final TechnicalFileService technicalFileService;
    private final SiteRepository siteRepository; // ✅ AJOUTER

    public StockService(StockModuleRepository stockRepository,
                        TechnicalFileService technicalFileService,  SiteRepository siteRepository) {
        this.stockRepository = stockRepository;
        this.technicalFileService = technicalFileService;
        this.siteRepository = siteRepository;

    }
    private Site getSiteFromTechnicalFileItem(TechnicalFileItem item) {
        try {
            if (item == null || item.getChargeSheetItem() == null ||
                    item.getChargeSheetItem().getChargeSheet() == null) {
                return null;
            }

            String plantName = item.getChargeSheetItem().getChargeSheet().getPlant();
            if (plantName == null) return null;

            // Chercher le site par nom
            return siteRepository.findByName(plantName).orElse(null);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du site: " + e.getMessage());
            return null;
        }
    }
    @Transactional
    public StockModule moveToStock(Long technicalFileId) {
        try {
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
        } catch (Exception e) {
            System.err.println("❌ Erreur dans moveToStock: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // Dans StockService.java
    // Dans StockService.java
    // StockService.java - Modifier la méthode moveItemToStock
    @Transactional
    public StockModule moveItemToStock(Long technicalFileItemId, StockModuleDto additionalInfo) {
        try {
            Optional<StockModule> existingStock = stockRepository.findByTechnicalFileItemId(technicalFileItemId);

            if (existingStock.isPresent()) {
                StockModule existing = existingStock.get();
                updateStockModuleWithDto(existing, additionalInfo);
                return stockRepository.save(existing);
            }

            TechnicalFileItem item = technicalFileService.getTechnicalFileItemById(technicalFileItemId);

            if (item.getValidationStatus() == null ||
                    item.getValidationStatus() == TechnicalFileItemStatus.DRAFT) {
                throw new RuntimeException("L'item doit être validé avant d'être déplacé en stock");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            // ✅ Récupérer le site de l'utilisateur
            Site userSite = currentUser.getSite();

            // Ou récupérer le site à partir du ChargeSheet
            Site siteFromItem = getSiteFromTechnicalFileItem(item);
            Site finalSite = userSite != null ? userSite : siteFromItem;

            Double finalDisplacement = getFinalDisplacement(item);
            Double finalProgrammedSealing = getFinalProgrammedSealing(item);
            String finalDetection = getFinalDetection(item);

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
                    // ✅ Ajouter le site
                    .site(finalSite)
                    // Informations supplémentaires du formulaire
                    .casier(additionalInfo.getCasier())
                    .stuffNumr(additionalInfo.getStuffNumr())
                    .leoniNumr(additionalInfo.getLeoniNumr())
                    .indexValue(additionalInfo.getIndexValue())
                    .quantite(additionalInfo.getQuantite())
                    .fournisseur(additionalInfo.getFournisseur())
                    .etat(additionalInfo.getEtat())
                    .caisse(additionalInfo.getCaisse())
                    .specifications(additionalInfo.getSpecifications())
                    .dernierMaj(LocalDate.now())
                    .infoSurModules(additionalInfo.getInfoSurModules())
                    .demandeurExplication(additionalInfo.getDemandeurExplication())
                    .dateDemande(additionalInfo.getDateDemande() != null ? additionalInfo.getDateDemande() : LocalDate.now())
                    .newQuantite(additionalInfo.getNewQuantite())
                    .build();

            return stockRepository.save(stock);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans moveItemToStock: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du déplacement en stock: " + e.getMessage());
        }
    }

    // Méthodes pour récupérer les dernières valeurs
    private Double getFinalDisplacement(TechnicalFileItem item) {
        // Priorité aux valeurs non nulles, en prenant la dernière modifiée
        // Si vous avez un historique, prenez la dernière valeur modifiée
        Double m1 = parseDoubleSafe(item.getDisplacementPathM1());
        Double m2 = parseDoubleSafe(item.getDisplacementPathM2());
        Double m3 = parseDoubleSafe(item.getDisplacementPathM3());

        // Retourner la valeur la plus récente (par exemple la plus grande)
        List<Double> values = new ArrayList<>();
        if (m1 != null) values.add(m1);
        if (m2 != null) values.add(m2);
        if (m3 != null) values.add(m3);

        return values.isEmpty() ? null : Collections.max(values);
    }

    private Double getFinalProgrammedSealing(TechnicalFileItem item) {
        Double m1 = parseDoubleSafe(item.getProgrammedSealingValueM1());
        Double m2 = parseDoubleSafe(item.getProgrammedSealingValueM2());
        Double m3 = parseDoubleSafe(item.getProgrammedSealingValueM3());

        List<Double> values = new ArrayList<>();
        if (m1 != null) values.add(m1);
        if (m2 != null) values.add(m2);
        if (m3 != null) values.add(m3);

        return values.isEmpty() ? null : Collections.max(values);
    }

    private String getFinalDetection(TechnicalFileItem item) {
        // Priorité à M3, puis M2, puis M1
        if (item.getDetectionsM3() != null && !item.getDetectionsM3().isEmpty())
            return item.getDetectionsM3();
        if (item.getDetectionsM2() != null && !item.getDetectionsM2().isEmpty())
            return item.getDetectionsM2();
        return item.getDetectionsM1();
    }

    private void updateStockModuleWithDto(StockModule module, StockModuleDto dto) {
        if (dto.getCasier() != null) module.setCasier(dto.getCasier());
        if (dto.getStuffNumr() != null) module.setStuffNumr(dto.getStuffNumr());
        if (dto.getLeoniNumr() != null) module.setLeoniNumr(dto.getLeoniNumr());
        if (dto.getIndexValue() != null) module.setIndexValue(dto.getIndexValue());
        if (dto.getQuantite() != null) module.setQuantite(dto.getQuantite());
        if (dto.getFournisseur() != null) module.setFournisseur(dto.getFournisseur());
        if (dto.getEtat() != null) module.setEtat(dto.getEtat());
        if (dto.getCaisse() != null) module.setCaisse(dto.getCaisse());
        if (dto.getSpecifications() != null) module.setSpecifications(dto.getSpecifications());
        if (dto.getInfoSurModules() != null) module.setInfoSurModules(dto.getInfoSurModules());
        if (dto.getDemandeurExplication() != null) module.setDemandeurExplication(dto.getDemandeurExplication());
        if (dto.getDateDemande() != null) module.setDateDemande(dto.getDateDemande());
        if (dto.getNewQuantite() != null) module.setNewQuantite(dto.getNewQuantite());
    }
    // Lister tous les modules
    // StockService.java - Modifier getAllStock()

    // StockService.java - Modifier la méthode getAllStock()
    // StockService.java - Corriger la méthode getAllStock()
    public List<StockModule> getAllStock() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            String userRole = currentUser.getRole().name();

            List<StockModule> allStock = stockRepository.findAll();

            // ADMIN voit tout
            if (userRole.equals("ADMIN")) {
                return allStock;
            }

            // ✅ Pour les autres rôles, filtrer uniquement par site de l'utilisateur
            // Pas de filtre par projet
            Long userSiteId = currentUser.getSite() != null ? currentUser.getSite().getId() : null;

            if (userSiteId == null) {
                System.err.println("⚠️ Utilisateur sans site associé: " + currentUser.getEmail());
                return new ArrayList<>();
            }

            return allStock.stream()
                    .filter(stock -> {
                        // Vérifier que le stock a un site
                        if (stock.getSite() == null) {
                            return false;
                        }
                        // Filtrer par site de l'utilisateur
                        return stock.getSite().getId().equals(userSiteId);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getAllStock: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Ajouter une méthode pour obtenir les stocks par site spécifique
    public List<StockModule> getStockBySite(String siteName) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            String userRole = currentUser.getRole().name();

            // Vérifier que l'utilisateur a accès à ce site
            if (!userRole.equals("ADMIN") && !currentUser.getSiteName().equals(siteName)) {
                throw new RuntimeException("Accès non autorisé à ce site");
            }

            return stockRepository.findAll().stream()
                    .filter(stock -> {
                        if (stock.getSite() == null) return false;
                        return siteName.equals(stock.getSite().getName());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getStockBySite: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Ajouter une méthode pour obtenir les statistiques par site
    // StockService.java - Corriger getStockStatistics()
    public Map<String, Object> getStockStatistics() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            String userRole = currentUser.getRole().name();

            List<StockModule> userStock = getAllStock(); // Déjà filtré par site

            // Calculer les statistiques
            long total = userStock.size();
            long available = userStock.stream()
                    .filter(s -> s.getStatus() == StockModule.StockStatus.AVAILABLE)
                    .count();
            long used = userStock.stream()
                    .filter(s -> s.getStatus() == StockModule.StockStatus.USED)
                    .count();
            long scrapped = userStock.stream()
                    .filter(s -> s.getStatus() == StockModule.StockStatus.SCRAPPED)
                    .count();

            // Compter par site (pour les stats détaillées)
            Map<String, Long> stockBySite = userStock.stream()
                    .filter(s -> s.getSite() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getSite().getName(),
                            Collectors.counting()
                    ));

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("available", available);
            stats.put("used", used);
            stats.put("scrapped", scrapped);
            stats.put("site", currentUser.getSiteName());
            stats.put("role", userRole);
            stats.put("stockBySite", stockBySite);

            return stats;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getStockStatistics: " + e.getMessage());
            return new HashMap<>();
        }
    }


    // Récupérer un module par ID
    public StockModule getStockById(Long id) {
        try {
            return stockRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Module stock non trouvé"));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getStockById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
        try {
            StockModule module = stockRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Module stock non trouvé"));
            module.setStatus(status);
            return stockRepository.save(module);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans changeStatus: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // StockService.java - Méthode createStockModule mise à jour

    // StockService.java - Modifier la méthode createStockModule
    // MODIFIER createStockModule
    @Transactional
    public StockModule createStockModule(StockModuleDto dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            TechnicalFile technicalFile = null;
            TechnicalFileItem technicalFileItem = null;

            if (dto.getTechnicalFileId() != null) {
                technicalFile = technicalFileService.getTechnicalFileById(dto.getTechnicalFileId());
            }

            if (dto.getTechnicalFileItemId() != null) {
                technicalFileItem = technicalFileService.getTechnicalFileItemById(dto.getTechnicalFileItemId());
            }

            // ✅ Récupérer le site
            Site site = null;
            if (dto.getSiteId() != null) {
                site = siteRepository.findById(dto.getSiteId()).orElse(null);
            } else if (currentUser.getSite() != null) {
                site = currentUser.getSite();
            } else if (technicalFileItem != null) {
                site = getSiteFromTechnicalFileItem(technicalFileItem);
            }

            StockModule stock = StockModule.builder()
                    .technicalFile(technicalFile)
                    .technicalFileItem(technicalFileItem)
                    .chargeSheetItemId(dto.getChargeSheetItemId())
                    .itemNumber(dto.getItemNumber())
                    .position(dto.getPosition())
                    .finalDisplacement(dto.getFinalDisplacement())
                    .finalProgrammedSealing(dto.getFinalProgrammedSealing())
                    .finalDetection(dto.getFinalDetection())
                    .movedBy(currentUser.getEmail())
                    .movedAt(LocalDate.now())
                    .status(dto.getStatus() != null ? dto.getStatus() : StockModule.StockStatus.AVAILABLE)
                    // ✅ Ajouter le site
                    .site(site)
                    .casier(dto.getCasier())
                    .stuffNumr(dto.getStuffNumr())
                    .leoniNumr(dto.getLeoniNumr())
                    .indexValue(dto.getIndexValue())
                    .quantite(dto.getQuantite())
                    .fournisseur(dto.getFournisseur())
                    .etat(dto.getEtat())
                    .caisse(dto.getCaisse())
                    .specifications(dto.getSpecifications())
                    .dernierMaj(dto.getDernierMaj() != null ? dto.getDernierMaj() : LocalDate.now())
                    .infoSurModules(dto.getInfoSurModules())
                    .demandeurExplication(dto.getDemandeurExplication())
                    .dateDemande(dto.getDateDemande() != null ? dto.getDateDemande() : LocalDate.now())
                    .newQuantite(dto.getNewQuantite())
                    .build();

            return stockRepository.save(stock);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans createStockModule: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Mettre à jour la méthode updateStockModule
    @Transactional
    public StockModule updateStockModule(Long id, StockModuleDto dto) {
        try {
            StockModule module = stockRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Module stock non trouvé"));

            // Mise à jour des champs existants
            if (dto.getStatus() != null) module.setStatus(dto.getStatus());
            if (dto.getFinalDisplacement() != null) module.setFinalDisplacement(dto.getFinalDisplacement());
            if (dto.getFinalProgrammedSealing() != null) module.setFinalProgrammedSealing(dto.getFinalProgrammedSealing());
            if (dto.getFinalDetection() != null) module.setFinalDetection(dto.getFinalDetection());

            // Mise à jour des nouveaux champs
            if (dto.getCasier() != null) module.setCasier(dto.getCasier());
            if (dto.getStuffNumr() != null) module.setStuffNumr(dto.getStuffNumr());
            if (dto.getLeoniNumr() != null) module.setLeoniNumr(dto.getLeoniNumr());
            if (dto.getIndexValue() != null) module.setIndexValue(dto.getIndexValue());
            if (dto.getQuantite() != null) module.setQuantite(dto.getQuantite());
            if (dto.getFournisseur() != null) module.setFournisseur(dto.getFournisseur());
            if (dto.getEtat() != null) module.setEtat(dto.getEtat());
            if (dto.getCaisse() != null) module.setCaisse(dto.getCaisse());
            if (dto.getSpecifications() != null) module.setSpecifications(dto.getSpecifications());
            if (dto.getDernierMaj() != null) module.setDernierMaj(dto.getDernierMaj());
            if (dto.getInfoSurModules() != null) module.setInfoSurModules(dto.getInfoSurModules());
            if (dto.getDemandeurExplication() != null) module.setDemandeurExplication(dto.getDemandeurExplication());
            if (dto.getDateDemande() != null) module.setDateDemande(dto.getDateDemande());  // ✅ Changé
            if (dto.getNewQuantite() != null) module.setNewQuantite(dto.getNewQuantite());

            return stockRepository.save(module);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateStockModule: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        return currentUser.getEmail();
    }
    // StockService.java - Ajouter cette méthode
    public Map<String, Object> getPreStockInfo(Long technicalFileItemId) {
        TechnicalFileItem item = technicalFileService.getTechnicalFileItemById(technicalFileItemId);

        Map<String, Object> info = new HashMap<>();
        info.put("technicalFileItemId", item.getId());
        info.put("itemNumber", item.getChargeSheetItem().getItemNumber());
        info.put("position", item.getPosition());

        // Dernières valeurs
        info.put("finalDisplacement", getFinalDisplacement(item));
        info.put("finalProgrammedSealing", getFinalProgrammedSealing(item));
        info.put("finalDetection", getFinalDetection(item));

        // Métadonnées
        info.put("chargeSheetItemId", item.getChargeSheetItem().getId());
        info.put("technicalFileId", item.getTechnicalFile().getId());

        return info;
    }
}
