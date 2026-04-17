package com.example.security.reception;


import com.example.security.cahierdeCharge.ChargeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charge-sheets/statistics")
@RequiredArgsConstructor
public class ReceptionStatisticsController {

    private final ChargeSheetService chargeSheetService;

    @GetMapping("/receptions")
    public ResponseEntity<ReceptionStatisticsDto> getReceptionStatistics(
            @RequestParam(required = false) String project,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "12") int months) {

        ReceptionStatisticsDto stats = chargeSheetService.getReceptionStatistics(project, site, months);
        return ResponseEntity.ok(stats);
    }
}
