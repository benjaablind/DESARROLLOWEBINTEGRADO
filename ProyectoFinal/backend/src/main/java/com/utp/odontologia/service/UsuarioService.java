package com.utp.odontologia.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.odontologia.dto.ActualizarUsuarioRequest;
import com.utp.odontologia.dto.CambiarPasswordRequest;
import com.utp.odontologia.dto.UsuarioRequest;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio de usuarios y autenticacion (Integrante 1).
 *
 * Semanas 8 y 9: las contrasenas se guardan cifradas con BCrypt y el inicio de
 * sesion lo resuelve Spring Security (ver AuthController y SecurityConfig).
 * Esta clase ya no compara contrasenas en texto plano.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /** Inyeccion por constructor: deja las dependencias explicitas y permite probar la clase. */
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lista usuarios aplicando filtros opcionales.
     * Cualquiera de los dos parametros puede ser null, lo que significa que no
     * se filtra por ese campo.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listar(Rol rol, Boolean activo) {
        return usuarioRepository.listarSi(u -> (rol == null || u.getRol() == rol)
                && (activo == null || u.isActivo() == activo));
    }

    /** Devuelve el usuario o lanza 404 si el id no existe. */
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", id));
    }

    /** Busca por nombre de usuario (el "sub" del token) o falla con 404. */
    @Transactional(readOnly = true)
    public Usuario buscarPorNombreDeUsuario(String usuario) {
        return usuarioRepository.buscarPorUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario con el nombre: " + usuario));
    }

    /**
     * Registra un usuario nuevo.
     * El nombre de usuario y el email son unicos porque ambos identifican a la persona.
     */
    public Usuario crear(UsuarioRequest request) {
        validarUsuarioDisponible(request.usuario(), null);
        validarEmailDisponible(request.email(), null);

        Usuario usuario = new Usuario(
                null,
                request.nombres().trim(),
                request.apellidos().trim(),
                request.email().trim(),
                request.usuario().trim(),
                // Solo se guarda el hash BCrypt, nunca la contrasena original.
                passwordEncoder.encode(request.password()),
                request.rol());

        return usuarioRepository.guardar(usuario);
    }

    /**
     * Actualiza los datos personales y el rol.
     * El nombre de usuario y la contrasena no se modifican aqui a proposito:
     * el primero identifica al usuario en el login y la segunda tiene su propio endpoint.
     */
    public Usuario actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarPorId(id);

        // El email debe seguir siendo unico, salvo que sea el del propio usuario editado.
        validarEmailDisponible(request.email(), id);

        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setEmail(request.email().trim());
        usuario.setRol(request.rol());

        return usuarioRepository.guardar(usuario);
    }

    /** Cambia la contrasena solo si la contrasena actual coincide. */
    public Usuario cambiarPassword(Long id, CambiarPasswordRequest request) {
        Usuario usuario = buscarPorId(id);

        // BCrypt no se puede "descifrar": se verifica si la contrasena coincide con el hash.
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contrasena actual no es correcta");
        }
        if (passwordEncoder.matches(request.passwordNuevo(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contrasena nueva debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNuevo()));
        return usuarioRepository.guardar(usuario);
    }

    /** Reactiva un usuario dado de baja. */
    public Usuario activar(Long id) {
        return cambiarEstado(id, true);
    }

    /**
     * Da de baja al usuario sin borrarlo, para conservar el historial de lo que registro.
     * Un usuario inactivo no puede iniciar sesion.
     */
    public Usuario desactivar(Long id) {
        return cambiarEstado(id, false);
    }

    /** Elimina el usuario o lanza 404 si el id no existe. */
    public void eliminar(Long id) {
        if (!usuarioRepository.existe(id)) {
            throw new RecursoNoEncontradoException("usuario", id);
        }
        usuarioRepository.eliminar(id);
    }

    /**
     * Permisos de cada rol, expresados con el formato modulo:accion.
     * El frontend los usa para armar el menu. Desde la semana 8 el servidor
     * aplica esta misma matriz en SecurityConfig.
     */
    @Transactional(readOnly = true)
    public List<String> permisosDe(Rol rol) {
        if (rol == null) {
            return List.of();
        }
        return switch (rol) {
            case ADMINISTRADOR -> List.of(
                    "usuarios:leer", "usuarios:escribir",
                    "pacientes:leer", "pacientes:escribir",
                    "citas:leer", "citas:escribir",
                    "historias:leer", "historias:escribir",
                    "odontograma:leer", "odontograma:escribir",
                    "tratamientos:leer", "tratamientos:escribir",
                    "pagos:leer", "pagos:escribir",
                    "reportes:leer", "reportes:escribir",
                    "archivos:leer", "archivos:escribir");
            case ODONTOLOGO -> List.of(
                    "pacientes:leer", "pacientes:escribir",
                    "citas:leer", "citas:escribir",
                    "historias:leer", "historias:escribir",
                    "odontograma:leer", "odontograma:escribir",
                    "tratamientos:leer", "tratamientos:escribir",
                    "archivos:leer", "archivos:escribir");
            case RECEPCIONISTA -> List.of(
                    "pacientes:leer", "pacientes:escribir",
                    "citas:leer", "citas:escribir",
                    "pagos:leer", "pagos:escribir",
                    "reportes:leer");
            // El asistente solo consulta: no puede modificar informacion clinica.
            case ASISTENTE -> List.of(
                    "pacientes:leer",
                    "citas:leer");
        };
    }

    private Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(activo);
        return usuarioRepository.guardar(usuario);
    }

    /**
     * El nombre de usuario no puede repetirse. El repositorio compara sin
     * distinguir mayusculas de minusculas.
     */
    private void validarUsuarioDisponible(String usuario, Long idPropio) {
        boolean ocupado = usuarioRepository.buscarPorUsuario(usuario)
                .filter(existente -> !existente.getId().equals(idPropio))
                .isPresent();

        if (ocupado) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario registrado con el nombre de usuario: " + usuario);
        }
    }

    /** El email tampoco puede repetirse entre usuarios distintos. */
    private void validarEmailDisponible(String email, Long idPropio) {
        boolean ocupado = usuarioRepository.buscarPorEmail(email)
                .filter(existente -> !existente.getId().equals(idPropio))
                .isPresent();

        if (ocupado) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario registrado con el email: " + email);
        }
    }
}
