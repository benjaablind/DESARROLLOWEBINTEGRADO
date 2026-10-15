package com.utp.productosapi.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp.productosapi.model.Rol;
import com.utp.productosapi.model.Usuario;
import com.utp.productosapi.repository.RolRepository;
import com.utp.productosapi.repository.UsuarioRepository;

/**
 * Crea los roles y los dos usuarios del laboratorio solo si todavia no
 * existen, de modo que reiniciar la aplicacion no duplique registros.
 *
 * Las contrasenas se codifican con BCrypt ANTES de guardarse: en MySQL solo
 * queda el hash. Estas credenciales son conocidas a proposito por tratarse de
 * un laboratorio; en un entorno real se aprovisionan con secretos.
 */
@Configuration
public class DatosSeguridadIniciales {

    @Bean
    CommandLineRunner cargarUsuarios(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                    .orElseGet(() -> rolRepository.save(new Rol("ROLE_USER")));
            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseGet(() -> rolRepository.save(new Rol("ROLE_ADMIN")));

            if (usuarioRepository.findByUsername("usuario").isEmpty()) {
                Usuario usuario = new Usuario(
                        "usuario",
                        passwordEncoder.encode("Usuario123*"),
                        true);
                usuario.setRoles(Set.of(rolUser));
                usuarioRepository.save(usuario);
            }

            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario(
                        "admin",
                        passwordEncoder.encode("Admin123*"),
                        true);
                admin.setRoles(Set.of(rolAdmin, rolUser));
                usuarioRepository.save(admin);
            }
        };
    }
}
