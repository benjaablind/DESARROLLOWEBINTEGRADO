#!/usr/bin/env bash
# Arranque del backend en Git Bash, Linux o macOS (semanas 6 a 9).
#
# La aplicacion necesita dos secretos que NO estan en el repositorio:
#   - DB_PASSWORD        contrasena de MySQL (usuario DB_USERNAME, por defecto root)
#   - JWT_SECRET_BASE64  clave con la que se firman los tokens (32 bytes en Base64)
#
# Uso, desde ProyectoFinal/backend:
#   ./iniciar.sh
#
# Antes, una sola vez: crear la base con sql/01_crear_base_datos.sql.

if [ -z "${DB_PASSWORD+x}" ]; then
    read -r -s -p "Contrasena de MySQL para '${DB_USERNAME:-root}': " DB_PASSWORD
    echo
    export DB_PASSWORD
fi

if [ -z "$JWT_SECRET_BASE64" ]; then
    # Cada arranque con una clave nueva invalida los tokens anteriores.
    JWT_SECRET_BASE64="$(openssl rand -base64 32)"
    export JWT_SECRET_BASE64
    echo "JWT_SECRET_BASE64 generada para esta sesion."
fi

./mvnw spring-boot:run
