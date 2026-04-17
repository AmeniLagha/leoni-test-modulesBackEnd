package com.example.security.config;

import com.example.security.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extraire TOUTES les authorities (rôles + permissions)
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Ajouter les authorities au token
        claims.put("authorities", authorities);

        // Séparer les rôles des permissions
        List<String> roles = authorities.stream()
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        List<String> permissions = authorities.stream()
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("permissions", permissions);

        // Ajouter le rôle principal
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("role", user.getRole().name());
            claims.put("firstname", user.getFirstname());
            claims.put("lastname", user.getLastname());
            // ✅ CORRECTION : Utiliser getProjetName() qui retourne String
            List<String> projetNames = user.getProjetNamesAsList();
            claims.put("projets", projetNames);  // Liste des projets

            // Pour compatibilité avec l'ancien code, garder un projet principal
            if (projetNames != null && !projetNames.isEmpty()) {
                claims.put("projet", projetNames.get(0));  // Premier projet
            }

            if (user.getSite() != null) {
                claims.put("siteName", user.getSite().getName());
            }
            // Optionnel : fullName
            claims.put("fullName", user.getFirstname() + " " + user.getLastname());
        }

        return generateToken(claims, userDetails);
    }


    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }



}
