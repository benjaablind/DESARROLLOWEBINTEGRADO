package com.utp.odontologia.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.utp.odontologia.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Respuestas 401 y 403 en el mismo formato JSON que el resto de la API
 * ({estado, mensaje, ruta, fechaHora}).
 *
 * Estos errores no pasan por ApiExceptionHandler: Spring Security los produce
 * en su cadena de filtros, antes de llegar a cualquier controlador.
 */
@Component
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    /** Agrega el encabezado estandar WWW-Authenticate: Bearer error="invalid_token"... */
    private final BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    public RespuestasDeSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 401: no hay token, o el token es invalido, alterado o esta vencido. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        bearer.commence(request, response, ex);
        escribir(response, new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                "Se requiere un token valido: inicie sesion en /api/auth/login", request.getRequestURI()));
    }

    /** 403: el token es valido, pero el rol no tiene permiso para esta operacion. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        escribir(response, new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                "Su rol no tiene permiso para realizar esta operacion", request.getRequestURI()));
    }

    private void escribir(HttpServletResponse response, ErrorResponse error) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
