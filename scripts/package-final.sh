#!/usr/bin/env bash
set -euo pipefail

output_dir="${1:-dist}"
package_name="${2:-SmartLogix_entrega_final}"
stage="$output_dir/$package_name"
zip_path="$output_dir/$package_name.zip"

rm -rf "$stage" "$zip_path"
mkdir -p "$stage"

exclude_args=(
  --exclude='.git'
  --exclude='.tools'
  --exclude='.vscode'
  --exclude='node_modules'
  --exclude='target'
  --exclude='dist'
  --exclude='build'
  --exclude='coverage'
  --exclude='logs'
  --exclude='generated'
  --exclude='.env'
  --exclude='*.env.local'
  --exclude='*.log'
  --exclude='*.zip'
  --exclude='*.rar'
  --exclude='*.7z'
  --exclude='*.tmp'
)

copy_tree() {
  local source="$1"
  local destination="$2"

  if [[ ! -e "$source" ]]; then
    printf 'Advertencia: no existe %s; se omite.\n' "$source" >&2
    return
  fi

  mkdir -p "$destination"
  if command -v rsync >/dev/null 2>&1; then
    rsync -a "${exclude_args[@]}" "$source"/ "$destination"/
  else
    tar "${exclude_args[@]}" -cf - -C "$source" . | tar -xf - -C "$destination"
  fi
}

copy_file() {
  local source="$1"
  local destination="$2"

  if [[ -f "$source" ]]; then
    mkdir -p "$(dirname "$destination")"
    cp "$source" "$destination"
  else
    printf 'Advertencia: no existe %s; se omite.\n' "$source" >&2
  fi
}

copy_file "README_ENTREGA.md" "$stage/README_ENTREGA.md"
copy_file "README.md" "$stage/README.md"
copy_file "README_TESTS.md" "$stage/README_TESTS.md"
copy_file "repositorios.txt" "$stage/repositorios.txt"
copy_file "docker-compose.yml" "$stage/docker-compose.yml"

copy_tree "docs" "$stage/01_documentacion/docs"
copy_tree "scripts" "$stage/scripts"
copy_tree "Frontend/smartlogix-app" "$stage/02_frontend/smartlogix-app"
copy_tree "Backend/api-gateway" "$stage/03_bff_api_gateway/api-gateway"

for service in auth users inventory order shipping notification eureka-server; do
  copy_tree "Backend/$service" "$stage/04_microservicios/$service"
done

copy_file "Backend/docker-compose-local.yml" "$stage/05_base_datos/docker-compose-local.yml"
copy_file "Backend/.env.example" "$stage/05_base_datos/Backend.env.example"
copy_tree "Backend/prisma" "$stage/05_base_datos/prisma"
copy_file "Backend/order/src/main/resources/data.sql" "$stage/05_base_datos/order-data.sql"

copy_file "README_TESTS.md" "$stage/06_pruebas/README_TESTS.md"
copy_file "docs/evidencias/04_pruebas_cobertura/cobertura-final.md" "$stage/06_pruebas/cobertura-final.md"
copy_tree "docs/evidencias" "$stage/07_evidencias"
copy_tree "docs/postman" "$stage/08_postman_openapi/postman"

if command -v zip >/dev/null 2>&1; then
  (cd "$output_dir" && zip -qr "$package_name.zip" "$package_name")
  printf 'Paquete generado: %s\n' "$zip_path"
else
  printf 'Advertencia: zip no esta instalado. Carpeta preparada en %s\n' "$stage" >&2
fi
