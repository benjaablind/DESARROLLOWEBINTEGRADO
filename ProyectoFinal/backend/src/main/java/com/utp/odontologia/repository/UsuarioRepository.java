package com.utp.odontologia.repository;

import java.util.List;
import java.util.Optional;

import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;

/**
 * Repositorio de usuarios (Integrante 1).
 *
 * Usa consultas derivadas: Spring Data arma la consulta a partir del nombre
 * del metodo (findByUsuarioIgnoreCase -> WHERE UPPER(usuario) = UPPER(?)).
 */
public interface UsuarioRepository extends RepositorioBase<Usuario> {

    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findByRolOrderById(Rol rol);

    List<Usuario> findByActivoTrueOrderById();

    // Con un parametro null, una consulta derivada buscaria "IS NULL"; aqui
    // null significa "sin criterio", asi que no se consulta.
    default Optional<Usuario> buscarPorUsuario(String usuario) {
        return usuario == null ? Optional.empty() : findByUsuarioIgnoreCase(usuario);
    }

    default Optional<Usuario> buscarPorEmail(String email) {
        return email == null ? Optional.empty() : findByEmailIgnoreCase(email);
    }

    default List<Usuario> listarPorRol(Rol rol) {
        return findByRolOrderById(rol);
    }

    default List<Usuario> listarActivos() {
        return findByActivoTrueOrderById();
    }
}
