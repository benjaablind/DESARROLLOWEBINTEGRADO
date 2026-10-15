# Laboratorio 09 - arranque en Windows PowerShell (seccion 4.1 de la guia).
#
# Genera 32 bytes criptograficamente aleatorios, los deja en JWT_SECRET_BASE64
# SOLO para esta sesion de terminal y arranca la API. La clave nunca se escribe
# en un archivo ni se sube a Git.
#
# Uso, desde la carpeta del proyecto:
#   .\iniciar.ps1
#
# Cada arranque genera una clave nueva, asi que los tokens emitidos antes dejan
# de ser validos: es lo esperado. Vuelve a hacer login.

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)

Write-Host "JWT_SECRET_BASE64 generada para esta sesion (32 bytes)."
.\mvnw.cmd spring-boot:run
