package com.utp.odontologia.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.utp.odontologia.model.Antecedentes;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.EstadoPieza;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.HistoriaClinica;
import com.utp.odontologia.model.MetodoPago;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.RegistroPieza;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.SesionTratamiento;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.HistoriaClinicaRepository;
import com.utp.odontologia.repository.OdontogramaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Carga datos de demostracion al arrancar la aplicacion.
 *
 * Mientras la persistencia sea en memoria, sin estos datos la aplicacion
 * arrancaria vacia en cada reinicio y el frontend no tendria nada que mostrar.
 * Cuando se incorpore PostgreSQL esta clase se reemplaza por un script de
 * migracion o por data.sql.
 *
 * Las pruebas limpian los repositorios que usan, asi que estos datos no
 * interfieren con ellas.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final CitaRepository citas;
    private final HistoriaClinicaRepository historias;
    private final OdontogramaRepository odontograma;
    private final TratamientoRepository tratamientos;
    private final PagoRepository pagos;

    public DatosIniciales(UsuarioRepository usuarios, PacienteRepository pacientes,
            CitaRepository citas, HistoriaClinicaRepository historias,
            OdontogramaRepository odontograma, TratamientoRepository tratamientos,
            PagoRepository pagos) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.citas = citas;
        this.historias = historias;
        this.odontograma = odontograma;
        this.tratamientos = tratamientos;
        this.pagos = pagos;
    }

    @Override
    public void run(String... args) {
        if (usuarios.contar() > 0) {
            return;
        }

        // --- Usuarios (Integrante 1) ---
        // La contrasena va en texto plano a proposito: el cifrado con BCrypt
        // llega junto con Spring Security en las semanas 6 a 10.
        Usuario admin = crearUsuario("Marco", "Gonzales del Valle", "admin@clinica.pe",
                "admin", "admin123", Rol.ADMINISTRADOR);
        Usuario odontologo = crearUsuario("Lucia", "Ramos Vega", "lramos@clinica.pe",
                "lramos", "lramos123", Rol.ODONTOLOGO);
        Usuario odontologo2 = crearUsuario("Diego", "Salas Ortiz", "dsalas@clinica.pe",
                "dsalas", "dsalas123", Rol.ODONTOLOGO);
        crearUsuario("Rosa", "Diaz Campos", "rdiaz@clinica.pe",
                "rdiaz", "rdiaz123", Rol.RECEPCIONISTA);
        crearUsuario("Jorge", "Palma Ruiz", "jpalma@clinica.pe",
                "jpalma", "jpalma123", Rol.ASISTENTE);

        // --- Pacientes (Integrante 2) ---
        Paciente ana = crearPaciente("72451890", "Ana Maria", "Torres Quispe",
                LocalDate.of(1992, 4, 18), "F", "987654321", "ana.torres@correo.pe",
                "Av. Arequipa 1520", "Lince", "Lima");
        ana.getAntecedentes().setAlergias(List.of("Penicilina"));
        ana.getAntecedentes().setHabitos(List.of("Bruxismo nocturno"));
        ana.getAntecedentes().setAntecedentesOdontologicos("Ortodoncia entre 2010 y 2012");

        Paciente carlos = crearPaciente("41236789", "Carlos Alberto", "Mendoza Rios",
                LocalDate.of(1985, 11, 3), "M", "951234876", "cmendoza@correo.pe",
                "Jr. Puno 340", "Cercado de Lima", "Lima");
        carlos.getAntecedentes().setEnfermedades(List.of("Hipertension arterial"));
        carlos.getAntecedentes().setMedicamentos(List.of("Losartan 50mg"));

        Paciente lucia = crearPaciente("70112345", "Lucia", "Fernandez Soto",
                LocalDate.of(2001, 7, 25), "F", "912345678", "lfernandez@correo.pe",
                "Calle Los Pinos 210", "Surco", "Lima");

        Paciente pedro = crearPaciente("09876543", "Pedro", "Huaman Ccahuana",
                LocalDate.of(1978, 2, 14), "M", "998877665", "phuaman@correo.pe",
                "Av. Brasil 890", "Magdalena", "Lima");
        pedro.getAntecedentes().setEnfermedades(List.of("Diabetes tipo 2"));
        pedro.getAntecedentes().setObservaciones("Requiere control de glucosa antes de cirugia");

        Paciente sofia = crearPaciente("75389012", "Sofia", "Ramirez Leon",
                LocalDate.of(2015, 9, 30), "F", "976543210", null,
                "Calle Bolivar 45", "Pueblo Libre", "Lima");

        Paciente jose = crearPaciente("43219876", "Jose Luis", "Cardenas Paredes",
                LocalDate.of(1969, 6, 8), "M", "934567812", "jcardenas@correo.pe",
                "Av. La Marina 1200", "San Miguel", "Lima");
        jose.setEstado(EstadoPaciente.INACTIVO);

        // --- Citas (Integrante 3) ---
        LocalDate hoy = LocalDate.now();
        crearCita(ana, odontologo, hoy.atTime(9, 0), 30, "Control de ortodoncia",
                EstadoCita.CONFIRMADA);
        crearCita(carlos, odontologo, hoy.atTime(10, 0), 45, "Dolor en molar inferior derecho",
                EstadoCita.PENDIENTE);
        crearCita(lucia, odontologo2, hoy.atTime(11, 30), 30, "Limpieza dental",
                EstadoCita.PENDIENTE);
        crearCita(pedro, odontologo, hoy.minusDays(3).atTime(16, 0), 60, "Evaluacion para implante",
                EstadoCita.ATENDIDA);
        crearCita(sofia, odontologo2, hoy.minusDays(1).atTime(15, 0), 30, "Control preventivo",
                EstadoCita.NO_ASISTIO);
        crearCita(ana, odontologo, hoy.plusDays(2).atTime(9, 30), 30, "Ajuste de brackets",
                EstadoCita.PENDIENTE);
        crearCita(carlos, odontologo2, hoy.plusDays(5).atTime(17, 0), 45, "Endodoncia primera sesion",
                EstadoCita.CONFIRMADA);

        // --- Historias clinicas (Integrante 4) ---
        crearHistoria(pedro, odontologo, hoy.minusDays(3).atTime(16, 0),
                "Evaluacion para implante en zona 46",
                "Paciente con diabetes controlada. Refiere perdida de pieza hace dos anos.",
                "Reborde alveolar conservado, encia sin signos de inflamacion.",
                "Edentulismo parcial en zona 46",
                "Toma de radiografia panoramica y planificacion quirurgica",
                "Implante unitario en pieza 46",
                "Amoxicilina 500mg cada 8 horas por 7 dias",
                "Se solicita control de hemoglobina glicosilada antes de la cirugia.");

        crearHistoria(ana, odontologo, hoy.minusDays(30).atTime(9, 0),
                "Control mensual de ortodoncia",
                "Sin molestias desde la ultima cita.",
                "Higiene adecuada, sin lesiones de caries visibles.",
                "Tratamiento ortodontico en curso, fase de alineamiento",
                "Cambio de arco superior e inferior",
                "Continua ortodoncia fija",
                null,
                "Se recomienda uso de placa de descarga por bruxismo.");

        crearHistoria(carlos, odontologo, hoy.minusDays(10).atTime(11, 0),
                "Dolor espontaneo en pieza 46",
                "Dolor pulsatil nocturno de una semana de evolucion.",
                "Caries profunda con compromiso pulpar en pieza 46. Percusion positiva.",
                "Pulpitis irreversible sintomatica en pieza 46",
                "Apertura camaral y medicacion intraconducto",
                "Endodoncia en pieza 46",
                "Ibuprofeno 600mg cada 8 horas por 3 dias",
                "Se programa continuacion del tratamiento en dos semanas.");

        // --- Odontograma (Integrante 4) ---
        crearRegistroPieza(carlos, odontologo, 46, EstadoPieza.CARIES, "Oclusal",
                "Caries profunda con compromiso pulpar", "Endodoncia",
                hoy.minusDays(10).atTime(11, 15));
        crearRegistroPieza(carlos, odontologo, 46, EstadoPieza.ENDODONCIA, "Oclusal",
                "Endodoncia en curso", "Obturacion definitiva pendiente",
                hoy.minusDays(3).atTime(11, 0));
        crearRegistroPieza(carlos, odontologo, 36, EstadoPieza.OBTURADO, "Oclusal",
                "Obturacion en buen estado", null, hoy.minusDays(10).atTime(11, 20));
        crearRegistroPieza(pedro, odontologo, 46, EstadoPieza.AUSENTE, null,
                "Pieza perdida hace dos anos", "Implante unitario",
                hoy.minusDays(3).atTime(16, 20));
        crearRegistroPieza(pedro, odontologo, 47, EstadoPieza.CORONA, null,
                "Corona metal ceramica en buen estado", null, hoy.minusDays(3).atTime(16, 25));
        crearRegistroPieza(ana, odontologo, 11, EstadoPieza.SANO, null, null, null,
                hoy.minusDays(30).atTime(9, 15));
        crearRegistroPieza(sofia, odontologo2, 55, EstadoPieza.SELLANTE, "Oclusal",
                "Sellante preventivo aplicado", null, hoy.minusDays(45).atTime(15, 30));

        // --- Tratamientos y pagos (Integrante 5) ---
        Tratamiento endodoncia = crearTratamiento(carlos, odontologo, "Endodoncia en pieza 46",
                "Tratamiento de conductos en molar inferior derecho", 850.00,
                EstadoTratamiento.EN_PROCESO, hoy.minusDays(10), null);
        agregarSesion(endodoncia, 1, hoy.minusDays(10), "Apertura camaral y medicacion", true);
        agregarSesion(endodoncia, 2, hoy.minusDays(3), "Instrumentacion de conductos", true);
        agregarSesion(endodoncia, 3, hoy.plusDays(5), "Obturacion de conductos", false);
        tratamientos.guardar(endodoncia);
        crearPago(endodoncia, 300.00, MetodoPago.EFECTIVO, hoy.minusDays(10).atTime(12, 0));
        crearPago(endodoncia, 250.00, MetodoPago.YAPE, hoy.minusDays(3).atTime(12, 0));

        Tratamiento ortodoncia = crearTratamiento(ana, odontologo, "Ortodoncia fija superior e inferior",
                "Tratamiento ortodontico completo con brackets metalicos", 4200.00,
                EstadoTratamiento.EN_PROCESO, hoy.minusMonths(8), null);
        agregarSesion(ortodoncia, 1, hoy.minusMonths(8), "Instalacion de brackets", true);
        agregarSesion(ortodoncia, 2, hoy.minusDays(30), "Cambio de arcos", true);
        agregarSesion(ortodoncia, 3, hoy.plusDays(2), "Ajuste de brackets", false);
        tratamientos.guardar(ortodoncia);
        crearPago(ortodoncia, 1500.00, MetodoPago.TARJETA, hoy.minusMonths(8).atTime(10, 0));
        crearPago(ortodoncia, 700.00, MetodoPago.TRANSFERENCIA, hoy.minusDays(30).atTime(10, 30));

        Tratamiento implante = crearTratamiento(pedro, odontologo, "Implante unitario en pieza 46",
                "Colocacion de implante con corona sobre implante", 3500.00,
                EstadoTratamiento.APROBADO, null, null);
        tratamientos.guardar(implante);
        crearPago(implante, 1000.00, MetodoPago.TRANSFERENCIA, hoy.minusDays(2).atTime(9, 0));

        Tratamiento limpieza = crearTratamiento(lucia, odontologo2, "Profilaxis dental",
                "Limpieza dental con destartraje y pulido", 120.00,
                EstadoTratamiento.COMPLETADO, hoy.minusDays(20), hoy.minusDays(20));
        agregarSesion(limpieza, 1, hoy.minusDays(20), "Destartraje y pulido", true);
        tratamientos.guardar(limpieza);
        crearPago(limpieza, 120.00, MetodoPago.EFECTIVO, hoy.minusDays(20).atTime(18, 0));

        Tratamiento sellantes = crearTratamiento(sofia, odontologo2, "Sellantes preventivos",
                "Aplicacion de sellantes en molares deciduos", 200.00,
                EstadoTratamiento.COMPLETADO, hoy.minusDays(45), hoy.minusDays(45));
        agregarSesion(sellantes, 1, hoy.minusDays(45), "Aplicacion de sellantes", true);
        tratamientos.guardar(sellantes);
        crearPago(sellantes, 200.00, MetodoPago.PLIN, hoy.minusDays(45).atTime(16, 0));

        Tratamiento blanqueamiento = crearTratamiento(lucia, odontologo2, "Blanqueamiento dental",
                "Blanqueamiento en consultorio", 600.00,
                EstadoTratamiento.PENDIENTE, null, null);
        tratamientos.guardar(blanqueamiento);
    }

    // ------------------------------------------------------------------
    // Utilitarios de construccion
    // ------------------------------------------------------------------

    private Usuario crearUsuario(String nombres, String apellidos, String email, String usuario,
            String password, Rol rol) {
        Usuario u = new Usuario(null, nombres, apellidos, email, usuario, password, rol);
        return usuarios.guardar(u);
    }

    private Paciente crearPaciente(String dni, String nombres, String apellidos,
            LocalDate fechaNacimiento, String sexo, String telefono, String email,
            String direccion, String distrito, String ciudad) {
        Paciente p = new Paciente();
        p.setDni(dni);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setFechaNacimiento(fechaNacimiento);
        p.setSexo(sexo);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setDireccion(direccion);
        p.setDistrito(distrito);
        p.setCiudad(ciudad);
        p.setAntecedentes(new Antecedentes());
        return pacientes.guardar(p);
    }

    private void crearCita(Paciente paciente, Usuario odontologo, LocalDateTime fechaHora,
            int duracion, String motivo, EstadoCita estado) {
        Cita c = new Cita();
        c.setPacienteId(paciente.getId());
        c.setOdontologoId(odontologo.getId());
        c.setFechaHora(fechaHora);
        c.setDuracionMinutos(duracion);
        c.setMotivo(motivo);
        c.setEstado(estado);
        citas.guardar(c);
    }

    private void crearHistoria(Paciente paciente, Usuario odontologo, LocalDateTime fecha,
            String motivo, String anamnesis, String examen, String diagnostico,
            String procedimiento, String tratamiento, String medicamentos, String observaciones) {
        HistoriaClinica h = new HistoriaClinica();
        h.setPacienteId(paciente.getId());
        h.setOdontologoId(odontologo.getId());
        h.setFecha(fecha);
        h.setMotivoConsulta(motivo);
        h.setAnamnesis(anamnesis);
        h.setExamenClinico(examen);
        h.setDiagnostico(diagnostico);
        h.setProcedimiento(procedimiento);
        h.setTratamiento(tratamiento);
        h.setMedicamentos(medicamentos);
        h.setObservaciones(observaciones);
        historias.guardar(h);
    }

    private void crearRegistroPieza(Paciente paciente, Usuario odontologo, int numeroPieza,
            EstadoPieza estado, String superficie, String diagnostico, String tratamiento,
            LocalDateTime fecha) {
        RegistroPieza r = new RegistroPieza();
        r.setPacienteId(paciente.getId());
        r.setOdontologoId(odontologo.getId());
        r.setNumeroPieza(numeroPieza);
        r.setEstado(estado);
        r.setSuperficie(superficie);
        r.setDiagnostico(diagnostico);
        r.setTratamiento(tratamiento);
        r.setFecha(fecha);
        odontograma.guardar(r);
    }

    private Tratamiento crearTratamiento(Paciente paciente, Usuario odontologo, String nombre,
            String descripcion, double precio, EstadoTratamiento estado,
            LocalDate fechaInicio, LocalDate fechaFin) {
        Tratamiento t = new Tratamiento();
        t.setPacienteId(paciente.getId());
        t.setOdontologoId(odontologo.getId());
        t.setNombre(nombre);
        t.setDescripcion(descripcion);
        t.setPrecio(precio);
        t.setEstado(estado);
        t.setFechaInicio(fechaInicio);
        t.setFechaFin(fechaFin);
        t.setSesiones(new ArrayList<>());
        return tratamientos.guardar(t);
    }

    private void agregarSesion(Tratamiento tratamiento, int numero, LocalDate fecha,
            String descripcion, boolean realizada) {
        SesionTratamiento s = new SesionTratamiento();
        s.setId((long) numero);
        s.setNumero(numero);
        s.setFecha(fecha);
        s.setDescripcion(descripcion);
        s.setRealizada(realizada);
        tratamiento.getSesiones().add(s);
    }

    private void crearPago(Tratamiento tratamiento, double monto, MetodoPago metodo,
            LocalDateTime fecha) {
        Pago p = new Pago();
        p.setTratamientoId(tratamiento.getId());
        p.setPacienteId(tratamiento.getPacienteId());
        p.setMonto(monto);
        p.setMetodo(metodo);
        p.setFecha(fecha);
        pagos.guardar(p);
    }
}
