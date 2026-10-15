package com.utp.odontologia;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Deja la base H2 de pruebas vacia antes de cada prueba.
 *
 * Con la persistencia en memoria cada prueba vaciaba solo los repositorios que
 * usaba. Con una base relacional eso ya no alcanza: las claves foraneas impiden
 * borrar un paciente que todavia tiene citas creadas por otra prueba. Por eso se
 * vacian todas las tablas a la vez, con la integridad referencial suspendida
 * solo durante la limpieza.
 *
 * RESTART IDENTITY reinicia los autoincrementales: cada prueba vuelve a ver ids
 * desde 1, igual que con el repositorio en memoria.
 */
@Component
public class LimpiadorBaseDatos {

    private final JdbcTemplate jdbc;

    public LimpiadorBaseDatos(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void limpiar() {
        List<String> tablas = jdbc.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                """, String.class);

        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        try {
            for (String tabla : tablas) {
                jdbc.execute("TRUNCATE TABLE " + tabla + " RESTART IDENTITY");
            }
        } finally {
            jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
