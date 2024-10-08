package com.PrathihasProjects.PrathihasSplitwise.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    SecretKey secret = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username, int f)
    {
        long timeLimit = 1000 * 60 * 60 * 10;

        if(f ==1)
            timeLimit = 1000 * 60 * 5;

        return Jwts.builder().setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeLimit)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }


    public Boolean validateToken(String token, String username) {
        final String usernameFromToken = getUsernameFromToken(token);
        return (usernameFromToken.equals(username) && !isTokenExpired(token));
    }

    public String getUsernameFromToken(String token) {

        try {
            return getClaimFromToken(token, Claims::getSubject);
        }
        catch(Exception e)
        {
            return "token not found";
        }
    }

    Boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
