# Laboratorio 10 - arranque en Windows PowerShell (seccion 10.1 de la guia).
#
# Deja los dos secretos SOLO en esta sesion de terminal y arranca la API:
#   - DB_PASSWORD: contrasena de MySQL. Con XAMPP el root no tiene: pulsa Enter.
#   - JWT_SECRET_BASE64: 32 bytes aleatorios en Base64, generados aqui.
# Ninguno se escribe en un archivo ni se sube a Git.
#
# Uso, desde la carpeta del proyecto:
#   .\iniciar.ps1                 (mvnw spring-boot:run)
#   .\iniciar.ps1 -Jar            (ejecuta el JAR de mvnw clean package)
#
# Cada arranque genera una clave JWT nueva: los tokens anteriores dejan de
# validar. Es lo esperado; vuelve a hacer login.

param([switch]$Jar)

if (-not $env:DB_PASSWORD) {
    $segura = Read-Host "Contrasena de MySQL para root (XAMPP: Enter)" -AsSecureString
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
}

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
Write-Host "JWT_SECRET_BASE64 generada para esta sesion (32 bytes)."

if ($Jar) {
    java -jar target\productos-api-0.0.1-SNAPSHOT.jar
} else {
    .\mvnw.cmd spring-boot:run
}
