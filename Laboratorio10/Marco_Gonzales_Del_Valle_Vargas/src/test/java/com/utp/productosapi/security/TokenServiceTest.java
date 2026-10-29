package com.utp.productosapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Seccion 12 de la guia: verifica la emision y decodificacion de un JWT sin
 * arrancar Spring ni MySQL. La clave es exclusiva de esta prueba.
 */
class TokenServiceTest {

    private static final SecretKey CLAVE = new SecretKeySpec(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(CLAVE));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(CLAVE)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

    private final TokenService service = new TokenService(encoder, "productos-api", 30);

    @Test
    void tokenContieneUsuarioYRolFirmados() {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = service.crearToken(auth);
        Jwt decoded = decoder.decode(token);

        assertEquals("admin", decoded.getSubject());
        // Se lee como texto: getIssuer() intenta convertir iss a URL y falla con
        // un emisor como "productos-api" (o el "tienda-api" de la guia).
        assertEquals("productos-api", decoded.getClaimAsString(JwtClaimNames.ISS));
        assertTrue(decoded.getClaimAsStringList("roles").contains("ROLE_ADMIN"));
        assertNotNull(decoded.getExpiresAt());
        assertEquals(30 * 60, decoded.getExpiresAt().getEpochSecond() - decoded.getIssuedAt().getEpochSecond());
    }

    @Test
    void tokenSoloIncluyeAutoridadesDeRol() {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "usuario", null, List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("FACTOR_PASSWORD")));

        Jwt decoded = decoder.decode(service.crearToken(auth));

        assertEquals(List.of("ROLE_USER"), decoded.getClaimAsStringList("roles"));
    }

    @Test
    void tokenConFirmaAlteradaDebeRechazarse() {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = service.crearToken(auth);

        // Se cambia el PRIMER caracter de la firma (tercer segmento). No el
        // ultimo: en una firma HS256 de 32 bytes, el ultimo caracter Base64URL
        // lleva bits de relleno y alterarlo podria no cambiar la firma real.
        int inicioFirma = token.lastIndexOf('.') + 1;
        char original = token.charAt(inicioFirma);
        String alterado = token.substring(0, inicioFirma)
                + (original == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        assertThrows(JwtException.class, () -> decoder.decode(alterado));
    }

    @Test
    void tokenFirmadoConOtraClaveDebeRechazarse() {
        SecretKey otraClave = new SecretKeySpec(
                "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        TokenService otroEmisor = new TokenService(
                new NimbusJwtEncoder(new ImmutableSecret<>(otraClave)), "productos-api", 30);
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String tokenAjeno = otroEmisor.crearToken(auth);

        assertThrows(JwtException.class, () -> decoder.decode(tokenAjeno));
    }
}
