param(
    [string]$OutputDir = "docs/final/pdf"
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

$ResolvedOutputDir = Join-Path $Root $OutputDir
New-Item -ItemType Directory -Force $ResolvedOutputDir | Out-Null

$Combined = Join-Path $ResolvedOutputDir "smartlogix-entrega-compilada.md"
$Html = Join-Path $ResolvedOutputDir "smartlogix-entrega-compilada.html"
$Pdf = Join-Path $ResolvedOutputDir "smartlogix-entrega-compilada.pdf"

$Docs = @(
    "README_ENTREGA.md",
    "README_TESTS.md",
    "docs/01-arquitectura.md",
    "docs/02-persistencia.md",
    "docs/03-pruebas-unitarias.md",
    "docs/04-despliegue-y-ejecucion.md",
    "docs/api-endpoints.md",
    "docs/demo-guion-funcional.md",
    "docs/guia-defensa-oral.md",
    "docs/final/informe-final-smartlogix.md",
    "docs/final/guion-defensa-smartlogix.md"
)

Set-Content -Path $Combined -Value "# SmartLogix - Entrega final`n" -Encoding utf8

foreach ($Doc in $Docs) {
    if (Test-Path $Doc) {
        Add-Content -Path $Combined -Value "`n---`n`n<!-- Fuente: $Doc -->`n" -Encoding utf8
        Add-Content -Path $Combined -Value (Get-Content -Path $Doc -Raw) -Encoding utf8
    }
    else {
        Write-Warning "No existe $Doc; se omite."
    }
}

Write-Host "Documento Markdown generado: $Combined"

$Pandoc = Get-Command pandoc -ErrorAction SilentlyContinue
if (-not $Pandoc) {
    Write-Warning "pandoc no esta instalado. Se genero solo el Markdown compilado."
    exit 0
}

& pandoc $Combined --standalone --toc -o $Html
Write-Host "Documento HTML generado: $Html"

try {
    & pandoc $Combined --standalone --toc -o $Pdf
    Write-Host "Documento PDF generado: $Pdf"
}
catch {
    Write-Warning "No se pudo generar PDF. Instala un motor LaTeX compatible o exporta el HTML/Markdown manualmente."
}
