package com.utp.odontologia.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.utp.odontologia.dto.ArchivoActualizarRequest;
import com.utp.odontologia.dto.ArchivoResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.TipoArchivo;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.ArchivoRepository;
import com.utp.odontologia.repository.HistoriaClinicaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio de los archivos clinicos (Integrante 6).
 *
 * Los metadatos viven en el repositorio en memoria, igual que en el resto de los
 * modulos de esta etapa; el contenido binario se escribe en la carpeta local
 * configurada en app.archivos.directorio.
 *
 * NOTAS DE ALCANCE (semanas 1 a 4 del curso):
 * - Todavia no hay control de acceso: cualquiera que conozca el id puede
 *   descargar un archivo. Cuando se incorpore Spring Security, la consulta, la
 *   descarga y el borrado quedaran sujetos a los permisos del rol del usuario
 *   autenticado, validando ademas que el paciente le corresponda.
 * - La carpeta local es una solucion de desarrollo. En la etapa de despliegue se
 *   reemplaza por almacenamiento en la nube (por ejemplo un bucket de objetos).
 *   Solo cambiara la forma de guardar, leer y borrar el binario: los metadatos y
 *   la API publica se mantienen igual.
 */
@Service
public class ArchivoService {

    private static final Logger LOG = LoggerFactory.getLogger(ArchivoService.class);

    /** Lista blanca de extensiones aceptadas por la clinica. */
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf", "doc", "docx", "txt");

    private final ArchivoRepository archivoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final String directorio;

    public ArchivoService(ArchivoRepository archivoRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            HistoriaClinicaRepository historiaClinicaRepository,
            @Value("${app.archivos.directorio}") String directorio) {

        this.archivoRepository = archivoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.directorio = directorio;
    }

    /**
     * Lista los archivos aplicando filtros opcionales.
     *
     * @param pacienteId id del paciente; puede ser null
     * @param tipo       tipo de documento; puede ser null
     */
    public List<Archivo> listar(Long pacienteId, TipoArchivo tipo) {
        return archivoRepository.listarSi(archivo -> {
            boolean coincidePaciente = pacienteId == null
                    || pacienteId.equals(archivo.getPacienteId());
            boolean coincideTipo = tipo == null || archivo.getTipo() == tipo;
            return coincidePaciente && coincideTipo;
        });
    }

    /** Archivos de un paciente, del mas reciente al mas antiguo. Falla con 404 si no existe. */
    public List<Archivo> listarPorPaciente(Long pacienteId) {
        validarPaciente(pacienteId);
        return archivoRepository.listarPorPaciente(pacienteId);
    }

    /** Busca los metadatos de un archivo o falla con 404. */
    public Archivo buscarPorId(Long id) {
        return archivoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("archivo", id));
    }

    /**
     * Registra un archivo clinico: valida, copia el binario a disco y guarda los metadatos.
     *
     * @param archivo           contenido recibido en el multipart
     * @param pacienteId        paciente duenio del archivo, obligatorio
     * @param usuarioId         usuario que lo sube, opcional
     * @param tipo              tipo de documento, obligatorio
     * @param descripcion       nota libre, opcional
     * @param historiaClinicaId consulta con la que se relaciona, opcional
     */
    public Archivo subir(MultipartFile archivo, Long pacienteId, Long usuarioId,
            TipoArchivo tipo, String descripcion, Long historiaClinicaId) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("El archivo esta vacio");
        }
        if (tipo == null) {
            throw new ReglaNegocioException("El tipo de archivo es obligatorio");
        }

        Paciente paciente = validarPaciente(pacienteId);
        validarUsuario(usuarioId);
        validarHistoriaClinica(historiaClinicaId);

        String nombreOriginal = sanearNombre(archivo.getOriginalFilename());
        String extension = extraerExtension(nombreOriginal);

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new ReglaNegocioException("La extension ." + extension + " no esta permitida");
        }

        // El nombre en disco se genera con UUID para que dos archivos con el mismo
        // nombre original nunca se pisen entre si.
        String nombreEnDisco = UUID.randomUUID() + "." + extension;
        Path destino = copiarADisco(archivo, nombreEnDisco);

        Archivo nuevo = new Archivo();
        nuevo.setPacienteId(paciente.getId());
        nuevo.setUsuarioId(usuarioId);
        nuevo.setHistoriaClinicaId(historiaClinicaId);
        nuevo.setTipo(tipo);
        nuevo.setNombre(nombreEnDisco);
        nuevo.setNombreOriginal(nombreOriginal);
        nuevo.setExtension(extension);
        nuevo.setContentType(archivo.getContentType());
        nuevo.setTamanoBytes(archivo.getSize());
        nuevo.setDescripcion(descripcion);
        nuevo.setUbicacion(destino.toString());

        return archivoRepository.guardar(nuevo);
    }

    /**
     * Entrega el contenido del archivo para descargarlo.
     * Falla con 404 si los metadatos existen pero el binario ya no esta en disco.
     */
    public Resource descargar(Long id) {
        Archivo archivo = buscarPorId(id);
        Path ruta = Paths.get(archivo.getUbicacion());

        try {
            Resource recurso = new UrlResource(ruta.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new RecursoNoEncontradoException("El archivo fisico ya no esta disponible");
            }
            return recurso;
        } catch (IOException ex) {
            throw new RecursoNoEncontradoException("El archivo fisico ya no esta disponible");
        }
    }

    /** Corrige los metadatos del archivo. El binario en disco no se toca nunca. */
    public Archivo actualizar(Long id, ArchivoActualizarRequest request) {
        Archivo archivo = buscarPorId(id);

        if (request.tipo() == null) {
            throw new ReglaNegocioException("El tipo de archivo es obligatorio");
        }
        validarHistoriaClinica(request.historiaClinicaId());

        archivo.setTipo(request.tipo());
        archivo.setDescripcion(request.descripcion());
        archivo.setHistoriaClinicaId(request.historiaClinicaId());

        return archivoRepository.guardar(archivo);
    }

    /**
     * Elimina los metadatos y el binario.
     * Si el borrado en disco falla se deja constancia en el log y la operacion
     * continua: el registro ya se elimino y no tiene sentido devolverle un error
     * al cliente por un archivo huerfano en el servidor.
     */
    public void eliminar(Long id) {
        Archivo archivo = buscarPorId(id);
        archivoRepository.eliminar(id);

        try {
            Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
        } catch (IOException | RuntimeException ex) {
            LOG.warn("No se pudo borrar el archivo fisico {}: {}",
                    archivo.getUbicacion(), ex.getMessage());
        }
    }

    /** Convierte la entidad en su DTO de salida resolviendo los nombres relacionados. */
    public ArchivoResponse aResponse(Archivo archivo) {
        String pacienteNombre = pacienteRepository.buscarPorId(archivo.getPacienteId())
                .map(Paciente::getNombreCompleto)
                .orElse(null);
        String usuarioNombre = usuarioRepository.buscarPorId(archivo.getUsuarioId())
                .map(Usuario::getNombreCompleto)
                .orElse(null);
        return ArchivoResponse.desde(archivo, pacienteNombre, usuarioNombre);
    }

    /** Convierte una lista completa de entidades a DTOs de salida. */
    public List<ArchivoResponse> aResponses(List<Archivo> archivos) {
        return archivos.stream().map(this::aResponse).toList();
    }

    /** Copia el contenido recibido a la carpeta configurada, creandola si hace falta. */
    private Path copiarADisco(MultipartFile archivo, String nombreEnDisco) {
        try {
            Path carpeta = Paths.get(directorio).toAbsolutePath().normalize();
            Files.createDirectories(carpeta);

            Path destino = carpeta.resolve(nombreEnDisco);
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return destino;
        } catch (IOException ex) {
            throw new ReglaNegocioException(
                    "No se pudo guardar el archivo en el servidor: " + ex.getMessage());
        }
    }

    /**
     * Deja el nombre original en una forma segura.
     * Rechaza los nombres que intentan escaparse de la carpeta de subidas.
     */
    private String sanearNombre(String nombreOriginal) {
        String nombre = nombreOriginal == null ? "" : nombreOriginal.trim();

        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre del archivo es obligatorio");
        }
        if (nombre.contains("..") || nombre.contains("/") || nombre.contains("\\")
                || nombre.indexOf(0) >= 0) {
            throw new ReglaNegocioException("El nombre del archivo no es valido");
        }
        return nombre;
    }

    /** Extension en minusculas, sin el punto. */
    private String extraerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto <= 0 || punto == nombre.length() - 1) {
            throw new ReglaNegocioException("El archivo debe tener una extension");
        }
        return nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    /** El paciente es obligatorio y debe existir. */
    private Paciente validarPaciente(Long pacienteId) {
        if (pacienteId == null) {
            throw new ReglaNegocioException("El paciente es obligatorio");
        }
        return pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", pacienteId));
    }

    /** El usuario es opcional, pero si se envia debe existir. */
    private void validarUsuario(Long usuarioId) {
        if (usuarioId != null && !usuarioRepository.existe(usuarioId)) {
            throw new RecursoNoEncontradoException("usuario", usuarioId);
        }
    }

    /** La consulta es opcional, pero si se envia debe existir. */
    private void validarHistoriaClinica(Long historiaClinicaId) {
        if (historiaClinicaId != null && !historiaClinicaRepository.existe(historiaClinicaId)) {
            throw new RecursoNoEncontradoException("historia clinica", historiaClinicaId);
        }
    }
}
