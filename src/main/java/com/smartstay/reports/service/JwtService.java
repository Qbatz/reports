package com.smartstay.reports.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private Key getKeyBySecret(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token, String secret) {
        return extractClaim(token, Claims::getSubject, secret);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver, String secret) {
        final Claims claims = extractAllClaims(token, secret);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaims(String token, String secret) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKeyBySecret(secret))
                    .build().parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException e) {
            throw new SignatureException("Token expired. Please login again.");
        } catch (MalformedJwtException e) {
            throw new SignatureException("Invalid token format. Please login again.");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new SignatureException("Signature mismatch. Please login again.");
        } catch (Exception e) {
            throw new SignatureException("Invalid token. Please login again.");
        }
    }

    public boolean validateServiceToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token, userDetails.getPassword());
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token, userDetails.getPassword()));
    }

    private boolean isTokenExpired(String token, String secret) {
        return extractExpiration(token, secret).before(new Date());
    }

    private Date extractExpiration(String token, String secret) {
        return extractClaim(token, Claims::getExpiration, secret);
    }
}
