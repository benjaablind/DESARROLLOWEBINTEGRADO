package com.utp.odontologia.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Puente entre la entidad Usuario de la base de datos y el UserDetails que
 * entiende Spring Security (semana 8). El framework no necesita conocer nuestro
 * modelo: solo pide un usuario por su nombre.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // El enum Rol guarda ADMINISTRADOR, ODONTOLOGO...; Spring Security espera
        // la autoridad con prefijo para que hasRole("ADMINISTRADOR") funcione.
        return User.withUsername(usuario.getUsuario())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}
