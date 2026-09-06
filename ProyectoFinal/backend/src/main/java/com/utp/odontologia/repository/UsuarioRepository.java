package com.utp.odontologia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;

/** Repositorio de usuarios (Integrante 1). */
@Repository
public class UsuarioRepository extends RepositorioEnMemoria<Usuario> {

    @Override
    protected Long obtenerId(Usuario entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Usuario entidad, Long id) {
        entidad.setId(id);
    }

    public Optional<Usuario> buscarPorUsuario(String usuario) {
        return listar().stream()
                .filter(u -> u.getUsuario().equalsIgnoreCase(usuario))
                .findFirst();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return listar().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return listarSi(u -> u.getRol() == rol);
    }

    public List<Usuario> listarActivos() {
        return listarSi(Usuario::isActivo);
    }
}
