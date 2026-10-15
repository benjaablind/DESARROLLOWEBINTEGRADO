package com.utp.odontologia.security;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Quien hace la peticion, segun el token (semana 9).
 *
 * Hasta la semana 5 el servidor no sabia quien llamaba y el cliente enviaba un
 * usuarioId en la peticion, que cualquiera podia falsificar. Ahora la identidad
 * sale del JWT ya verificado (su claim "sub" es el nombre de usuario).
 *
 * Se registra como bean "usuarioActual" para usarlo en expresiones de
 * seguridad: @PreAuthorize("... or @usuarioActual.es(#id)").
 */
@Component("usuarioActual")
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepository.buscarPorUsuario(autenticacion.getName());
    }

    /** Id del usuario autenticado, o null si no se puede resolver. */
    public Long id() {
        return obtener().map(Usuario::getId).orElse(null);
    }

    /** true si el id indicado es el del propio usuario autenticado. */
    public boolean es(Long id) {
        return id != null && id.equals(id());
    }
}
