package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("service", "Leoni Backend API");
        status.put("version", "1.0.0");

        // Vérifier la base de données
        Map<String, Object> dbStatus = checkDatabase();
        status.put("database", dbStatus);

        // Vérifier si le service est globalement healthy
        boolean isHealthy = "UP".equals(dbStatus.get("status"));
        status.put("healthy", isHealthy);

        return isHealthy ? ResponseEntity.ok(status) : ResponseEntity.status(503).body(status);
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbStatus = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // Vérifier la connexion
            dbStatus.put("status", "UP");
            dbStatus.put("url", conn.getMetaData().getURL());
            dbStatus.put("product", conn.getMetaData().getDatabaseProductName());
            dbStatus.put("version", conn.getMetaData().getDatabaseProductVersion());

            // Vérifier que la BDD répond aux requêtes
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT 1")) {
                if (rs.next()) {
                    dbStatus.put("query_test", "OK");
                }
            }

        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
            dbStatus.put("error_type", e.getClass().getSimpleName());
        }

        return dbStatus;
    }
}