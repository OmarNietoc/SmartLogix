#!/usr/bin/env bash
set -euo pipefail

output_dir="${1:-docs/final/pdf}"
mkdir -p "$output_dir"

combined="$output_dir/smartlogix-entrega-compilada.md"
html="$output_dir/smartlogix-entrega-compilada.html"
pdf="$output_dir/smartlogix-entrega-compilada.pdf"

docs=(
  "README_ENTREGA.md"
  "README_TESTS.md"
  "docs/01-arquitectura.md"
  "docs/02-persistencia.md"
  "docs/03-pruebas-unitarias.md"
  "docs/04-despliegue-y-ejecucion.md"
  "docs/api-endpoints.md"
  "docs/demo-guion-funcional.md"
  "docs/guia-defensa-oral.md"
  "docs/final/informe-final-smartlogix.md"
  "docs/final/guion-defensa-smartlogix.md"
)

printf '# SmartLogix - Entrega final\n\n' > "$combined"

for doc in "${docs[@]}"; do
  if [[ -f "$doc" ]]; then
    {
      printf '\n---\n\n<!-- Fuente: %s -->\n\n' "$doc"
      cat "$doc"
      printf '\n'
    } >> "$combined"
  else
    printf 'Advertencia: no existe %s; se omite.\n' "$doc" >&2
  fi
done

printf 'Documento Markdown generado: %s\n' "$combined"

if ! command -v pandoc >/dev/null 2>&1; then
  printf 'Advertencia: pandoc no esta instalado. Se genero solo el Markdown compilado.\n' >&2
  exit 0
fi

pandoc "$combined" --standalone --toc -o "$html"
printf 'Documento HTML generado: %s\n' "$html"

if pandoc "$combined" --standalone --toc -o "$pdf"; then
  printf 'Documento PDF generado: %s\n' "$pdf"
else
  printf 'Advertencia: no se pudo generar PDF. Instala un motor LaTeX compatible o exporta el HTML/Markdown manualmente.\n' >&2
fi
