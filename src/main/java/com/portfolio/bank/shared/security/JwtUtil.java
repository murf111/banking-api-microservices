package com.portfolio.bank.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Security class for managing JSON Web Tokens (JWT).
 *
 * <p>Responsible for:</p>
 * <ul>
 * <li>Generating signed tokens for authenticated users</li>
 * <li>Extracting claims (username, expiration) from tokens</li>
 * <li>Validating token integrity and expiration status</li>
 * </ul>
 *
 * <p>Uses HMAC SHA-256 encryption.</p>
 *
 * @author Denys Sych
 * @version 1.0
 * @since 2026
 */
@Component
public class JwtUtil {

    // Default key for DEV only. In PROD, set JWT_SECRET env variable.
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}") // 1 Hour
    private long jwtExpiration;

    /**
     * Extracts the username (subject) from a valid JWT.
     *
     * @param token the JWT string
     * @return the username embedded in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token the JWT string
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a new JWT for the provided user details.
     *
     * @param userDetails the authenticated user information
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a new JWT with custom claims.
     *
     * @param extraClaims map of additional claims to include
     * @param userDetails the authenticated user information
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                   .setClaims(extraClaims)
                   .setSubject(userDetails.getUsername())
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                   .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    /**
     * Validates if a token belongs to the given user and is not expired.
     *
     * @param token the JWT string
     * @param userDetails the user to check against
     * @return true if valid and matching, false otherwise
     */
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
        return Jwts.parserBuilder()
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