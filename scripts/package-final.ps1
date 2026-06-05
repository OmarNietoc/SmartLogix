param(
    [string]$OutputDir = "dist",
    [string]$PackageName = "SmartLogix_entrega_final"
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

$Dist = Join-Path $Root $OutputDir
$Stage = Join-Path $Dist $PackageName
$Zip = Join-Path $Dist "$PackageName.zip"

function Assert-InsideRoot {
    param([string]$Path)

    $FullPath = [System.IO.Path]::GetFullPath($Path)
    $RootPath = [System.IO.Path]::GetFullPath($Root)
    if (-not $FullPath.StartsWith($RootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Ruta fuera del workspace: $FullPath"
    }
}

Assert-InsideRoot $Dist
Assert-InsideRoot $Stage
Assert-InsideRoot $Zip

New-Item -ItemType Directory -Force $Dist | Out-Null
if (Test-Path $Stage) {
    Remove-Item -LiteralPath $Stage -Recurse -Force
}
if (Test-Path $Zip) {
    Remove-Item -LiteralPath $Zip -Force
}
New-Item -ItemType Directory -Force $Stage | Out-Null

$ExcludedDirs = @(".git", ".tools", ".vscode", "node_modules", "target", "dist", "build", "coverage", "logs", "generated")
$ExcludedFiles = @(".env")
$ExcludedFilePatterns = @("*.env.local", "*.log", "*.zip", "*.rar", "*.7z", "*.tmp")

function Test-IsExcluded {
    param(
        [System.IO.FileSystemInfo]$Item,
        [string]$SourceRoot
    )

    $Relative = $Item.FullName.Substring($SourceRoot.Length).TrimStart("\", "/")
    $Segments = $Relative -split "[\\/]+"

    foreach ($Segment in $Segments) {
        if ($ExcludedDirs -contains $Segment) {
            return $true
        }
    }

    if (-not $Item.PSIsContainer) {
        if ($ExcludedFiles -contains $Item.Name) {
            return $true
        }
        foreach ($Pattern in $ExcludedFilePatterns) {
            if ($Item.Name -like $Pattern) {
                return $true
            }
        }
    }

    return $false
}

function Copy-TreeFiltered {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (-not (Test-Path $Source)) {
        Write-Warning "No existe $Source; se omite."
        return
    }

    $SourceRoot = (Resolve-Path $Source).Path
    New-Item -ItemType Directory -Force $Destination | Out-Null

    Get-ChildItem -LiteralPath $SourceRoot -Recurse -Force | ForEach-Object {
        if (Test-IsExcluded -Item $_ -SourceRoot $SourceRoot) {
            return
        }

        $Relative = $_.FullName.Substring($SourceRoot.Length).TrimStart("\", "/")
        $Target = Join-Path $Destination $Relative

        if ($_.PSIsContainer) {
            New-Item -ItemType Directory -Force $Target | Out-Null
        }
        else {
            $Parent = Split-Path -Parent $Target
            New-Item -ItemType Directory -Force $Parent | Out-Null
            Copy-Item -LiteralPath $_.FullName -Destination $Target -Force
        }
    }
}

function Copy-FileIfExists {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (Test-Path $Source) {
        $Parent = Split-Path -Parent $Destination
        New-Item -ItemType Directory -Force $Parent | Out-Null
        Copy-Item -LiteralPath $Source -Destination $Destination -Force
    }
    else {
        Write-Warning "No existe $Source; se omite."
    }
}

Copy-FileIfExists "README_ENTREGA.md" (Join-Path $Stage "README_ENTREGA.md")
Copy-FileIfExists "README.md" (Join-Path $Stage "README.md")
Copy-FileIfExists "README_TESTS.md" (Join-Path $Stage "README_TESTS.md")
Copy-FileIfExists "repositorios.txt" (Join-Path $Stage "repositorios.txt")
Copy-FileIfExists "docker-compose.yml" (Join-Path $Stage "docker-compose.yml")

Copy-TreeFiltered "docs" (Join-Path $Stage "01_documentacion/docs")
Copy-TreeFiltered "scripts" (Join-Path $Stage "scripts")
Copy-TreeFiltered "Frontend/smartlogix-app" (Join-Path $Stage "02_frontend/smartlogix-app")
Copy-TreeFiltered "Backend/api-gateway" (Join-Path $Stage "03_bff_api_gateway/api-gateway")

$Services = @("auth", "users", "inventory", "order", "shipping", "notification", "eureka-server")
foreach ($Service in $Services) {
    Copy-TreeFiltered "Backend/$Service" (Join-Path $Stage "04_microservicios/$Service")
}

Copy-FileIfExists "Backend/docker-compose-local.yml" (Join-Path $Stage "05_base_datos/docker-compose-local.yml")
Copy-FileIfExists "Backend/.env.example" (Join-Path $Stage "05_base_datos/Backend.env.example")
Copy-TreeFiltered "Backend/prisma" (Join-Path $Stage "05_base_datos/prisma")
Copy-FileIfExists "Backend/order/src/main/resources/data.sql" (Join-Path $Stage "05_base_datos/order-data.sql")

Copy-FileIfExists "README_TESTS.md" (Join-Path $Stage "06_pruebas/README_TESTS.md")
Copy-FileIfExists "docs/evidencias/04_pruebas_cobertura/cobertura-final.md" (Join-Path $Stage "06_pruebas/cobertura-final.md")
Copy-TreeFiltered "docs/evidencias" (Join-Path $Stage "07_evidencias")
Copy-TreeFiltered "docs/postman" (Join-Path $Stage "08_postman_openapi/postman")

Compress-Archive -Path (Join-Path $Stage "*") -DestinationPath $Zip -Force

Write-Host "Paquete generado: $Zip"
