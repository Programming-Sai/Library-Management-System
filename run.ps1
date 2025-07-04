.\gradlew installDist


$script = Join-Path $PSScriptRoot "app\build\install\app\bin\app.bat"

if (Test-Path $script) {
    cmd /c "$script $args"
} else {
    Write-Error "App script not found: $script"
}
