# Evidencia 07 - GitHub

## Que evidencia debe ir aqui

- Captura del repositorio GitHub.
- Captura de rama o release usado para entrega.
- Captura de GitHub Actions si existe ejecucion verde.
- Captura de `git status --short` limpio o con cambios documentales esperados.
- Captura de `git log --oneline -5`.

## Como generarla

```bash
git status --short
git log --oneline -5
```

En Windows, si Git no esta en PATH, usar la ruta instalada, por ejemplo:

```powershell
& "C:\Program Files\Git\bin\git.exe" status --short
& "C:\Program Files\Git\bin\git.exe" log --oneline -5
```

## Archivo o captura a usar

- `repositorios.txt`.
- Captura de GitHub.
- Captura de acciones CI/CD si estan disponibles.

## Por que sirve para la evaluacion

Permite verificar trazabilidad, repositorio entregado y estado final del codigo/documentacion.
