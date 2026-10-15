# Arranque del backend en Windows PowerShell (semanas 6 a 9).
#
# La aplicacion necesita dos secretos que NO estan en el repositorio:
#   - DB_PASSWORD        contrasena de MySQL (usuario DB_USERNAME, por defecto root)
#   - JWT_SECRET_BASE64  clave con la que se firman los tokens (32 bytes en Base64)
#
# Este script los deja solo en la sesion de terminal actual y arranca la API.
# Uso, desde ProyectoFinal/backend:
#   .\iniciar.ps1
#
# Antes, una sola vez: crear la base con sql/01_crear_base_datos.sql.

if (-not $env:DB_PASSWORD) {
    $segura = Read-Host "Contrasena de MySQL para '$(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'root' })'" -AsSecureString
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
}

if (-not $env:JWT_SECRET_BASE64) {
    # 32 bytes de un generador criptografico. Cada arranque con una clave nueva
    # invalida los tokens anteriores: es lo esperado, basta con volver a hacer login.
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
    Write-Host "JWT_SECRET_BASE64 generada para esta sesion."
}

.\mvnw.cmd spring-boot:run
