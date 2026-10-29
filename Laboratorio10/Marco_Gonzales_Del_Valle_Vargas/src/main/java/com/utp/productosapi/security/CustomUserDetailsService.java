package com.utp.productosapi.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.utp.productosapi.model.Usuario;
import com.utp.productosapi.repository.UsuarioRepository;

/**
 * Puente entre la entidad Usuario de MySQL y el UserDetails que Spring
 * Security entiende. Gracias a esta abstraccion, el framework no necesita
 * conocer nuestro modelo de datos.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Los roles ya se guardan como ROLE_USER / ROLE_ADMIN, por eso se
        // registran como autoridades tal cual, sin anadir prefijo.
        String[] autoridades = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .toArray(String[]::new);

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(autoridades)
                .disabled(!usuario.isActivo())
                .build();
    }
}
