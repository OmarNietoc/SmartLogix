# Diagramas finales

Las fuentes Mermaid oficiales estan en:

```text
docs/diagrams/
```

Archivos:

- `arquitectura-microservicios.mmd`
- `despliegue.mmd`
- `secuencia-creacion-orden.mmd`
- `er-simplificado.mmd`

## Exportar a SVG o PNG

Con Mermaid CLI:

```bash
npx @mermaid-js/mermaid-cli -i docs/diagrams/arquitectura-microservicios.mmd -o docs/final/diagrams/arquitectura-microservicios.svg
npx @mermaid-js/mermaid-cli -i docs/diagrams/despliegue.mmd -o docs/final/diagrams/despliegue.svg
npx @mermaid-js/mermaid-cli -i docs/diagrams/secuencia-creacion-orden.mmd -o docs/final/diagrams/secuencia-creacion-orden.svg
npx @mermaid-js/mermaid-cli -i docs/diagrams/er-simplificado.mmd -o docs/final/diagrams/er-simplificado.svg
```

Si no se usa Mermaid CLI, los `.mmd` pueden pegarse en Mermaid Live Editor y descargarse como SVG/PNG para adjuntarlos como evidencia.
