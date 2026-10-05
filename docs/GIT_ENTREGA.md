# Preparación del repositorio GIT público

## Requisito

El repositorio debe ser público, deben verse commits de todos los integrantes y cada integrante debe tener una rama propia.

## Flujo recomendado

```bash
git init
git add .
git commit -m "feat: base funcional Mi Puente Financiero"
git branch -M main
```

Después de crear el repositorio remoto:

```bash
git remote add origin URL_DEL_REPOSITORIO
git push -u origin main
```

Cada integrante crea su rama:

```bash
git checkout -b nombre-integrante
git push -u origin nombre-integrante
```

Cada persona debe hacer al menos uno o varios cambios reales desde su rama y generar commits con su propia cuenta. Después se integran por Pull Request o merge a `main`.

## Ejemplo de división

- Integrante 1: persistencia SQLite / Repository.
- Integrante 2: interfaz y Material 3.
- Integrante 3: servicio en línea, pantalla Acerca de y documentación.

No hagan commits ficticios ni cambien autorías: la rúbrica exige evidencia real de colaboración.
