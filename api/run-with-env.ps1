# Loads api/.env into the process and starts Spring Boot.
# Usage:  cd api; .\run-with-env.ps1

$ErrorActionPreference = 'Stop'
$apiRoot = $PSScriptRoot
$envFile = Join-Path $apiRoot '.env'

if (-not (Test-Path $envFile)) {
  Write-Error 'Missing .env — copy env.example to .env and fill Neon credentials.'
}

Get-Content $envFile | ForEach-Object {
  $line = $_.Trim()
  if (-not $line -or $line.StartsWith('#')) { return }
  $eq = $line.IndexOf('=')
  if ($eq -lt 1) { return }
  $name = $line.Substring(0, $eq).Trim()
  $value = $line.Substring($eq + 1).Trim().Trim('"').Trim("'")
  Set-Item -Path ('Env:' + $name) -Value $value
}

if (-not $env:JAVA_HOME) {
  $env:JAVA_HOME = 'C:\Program Files\Microsoft\jdk-25.0.4.101-hotspot'
}
$env:Path = $env:JAVA_HOME + '\bin;C:\tools\apache-maven-3.9.11\bin;' + $env:Path
$env:MAVEN_OPTS = '-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NUL'

Write-Host ('Datasource: ' + $env:SPRING_DATASOURCE_URL)
Set-Location $apiRoot
& mvn -q spring-boot:run
