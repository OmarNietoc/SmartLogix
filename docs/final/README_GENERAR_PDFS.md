# Generacion de documentos finales

Esta carpeta contiene fuentes Markdown listas para imprimir o convertir a PDF.

## Archivos principales

| Archivo | Uso |
|---|---|
| `informe-final-smartlogix.md` | Informe tecnico consolidado |
| `guion-defensa-smartlogix.md` | Guion breve para defensa oral |
| `diagrams/README.md` | Instrucciones para exportar diagramas Mermaid |

## Generar documento compilado

Desde la raiz del proyecto:

```powershell
scripts/generate-docs.ps1
```

o en Bash:

```bash
bash scripts/generate-docs.sh
```

Los archivos quedan en:

```text
docs/final/pdf/
```

El script siempre genera `smartlogix-entrega-compilada.md`. Si `pandoc` esta disponible, tambien intenta generar HTML y PDF.

## Dependencias opcionales

Para PDF automatico se recomienda instalar:

- `pandoc`
- Un motor LaTeX compatible, por ejemplo MiKTeX o TeX Live

Si no estan instalados, se puede abrir el Markdown compilado en VS Code, Word, Typora u otro editor y exportar manualmente a PDF.
