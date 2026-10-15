package com.utp.productosapi.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emite el JWT a partir de un Authentication YA validado. No recibe la
 * contrasena ni consulta MySQL: sus entradas son la identidad autenticada y
 * las autoridades que Spring Security le otorgo.
 *
 * El token va firmado, NO cifrado: cualquiera puede leer el payload en
 * Base64URL. Por eso solo lleva sub, iss, iat, exp y roles.
 */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final long ttlMinutes;

    public TokenService(JwtEncoder encoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-minutes:30}") long ttlMinutes) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public long expiresInSeconds() {
        return ttlMinutes * 60;
    }

    public String crearToken(Authentication autenticacion) {
        Instant ahora = Instant.now();

        List<String> roles = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(autenticacion.getName())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(ttlMinutes, ChronoUnit.MINUTES))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
