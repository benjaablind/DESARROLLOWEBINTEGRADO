package com.utp.odontologia.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.ActualizarUsuarioRequest;
import com.utp.odontologia.dto.CambiarPasswordRequest;
import com.utp.odontologia.dto.LoginRequest;
import com.utp.odontologia.dto.UsuarioRequest;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio de usuarios y autenticacion (Integrante 1).
 *
 * En esta etapa del curso los datos viven en memoria y las contrasenas se
 * comparan en texto plano. El cifrado con BCrypt, Spring Security y los tokens
 * JWT entran en las semanas 6 a 10 y solo afectaran a esta clase.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /** Inyeccion por constructor: deja la dependencia explicita y permite probar la clase. */
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista usuarios aplicando filtros opcionales.
     * Cualquiera de los dos parametros puede ser null, lo que significa que no
     * se filtra por ese campo.
     */
    public List<Usuario> listar(Rol rol, Boolean activo) {
        return usuarioRepository.listarSi(u -> (rol == null || u.getRol() == rol)
                && (activo == null || u.isActivo() == activo));
    }

    /** Devuelve el usuario o lanza 404 si el id no existe. */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", id));
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
                request.password(),
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

        // Comparacion directa porque en esta etapa no hay cifrado.
        if (!request.passwordActual().equals(usuario.getPassword())) {
            throw new ReglaNegocioException("La contrasena actual no es correcta");
        }
        if (request.passwordNuevo().equals(usuario.getPassword())) {
            throw new ReglaNegocioException("La contrasena nueva debe ser distinta de la actual");
        }

        usuario.setPassword(request.passwordNuevo());
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
     * Verifica las credenciales del inicio de sesion.
     *
     * Se resuelve comparando el nombre de usuario y la contrasena guardados en
     * memoria. Spring Security y JWT entran en las semanas 6 a 10 del curso;
     * hasta entonces no se emite ningun token ni se guarda sesion en el servidor.
     */
    public Usuario autenticar(LoginRequest request) {
        Optional<Usuario> encontrado = usuarioRepository.buscarPorUsuario(request.usuario());

        // Se usa el mismo mensaje para usuario inexistente y para contrasena
        // incorrecta, para no revelar que nombres de usuario existen.
        Usuario usuario = encontrado
                .orElseThrow(() -> new ReglaNegocioException("Usuario o contrasena incorrectos"));

        if (!request.password().equals(usuario.getPassword())) {
            throw new ReglaNegocioException("Usuario o contrasena incorrectos");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario esta inactivo");
        }

        return usuario;
    }

    /**
     * Permisos de cada rol, expresados con el formato modulo:accion.
     * El frontend los usa para armar el menu; la verificacion en el servidor
     * llegara con Spring Security.
     */
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
