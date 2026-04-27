# NotasApp · Calculadora de promedio académico

Aplicación Android desarrollada con **Jetpack Compose**, **Kotlin**, **Room** y **Navigation Compose** que permite a un estudiante:

1. Registrarse e iniciar sesión.
2. Ingresar las materias cursadas con su nota correspondiente (escala **0 – 10**).
3. Calcular automáticamente el promedio.
4. Determinar si **aprobó** (≥ 6.0) o **reprobó** y mostrarlo en una pantalla dedicada.

---

## Tecnologías utilizadas

| Capa | Tecnología |
| --- | --- |
| Lenguaje | Kotlin 1.9 |
| UI | Jetpack Compose · Material 3 |
| Arquitectura | MVVM con `ViewModel` + `StateFlow` |
| Persistencia | Room (SQLite) |
| Navegación | Navigation Compose |
| Asincronía | Kotlin Coroutines |
| SDK objetivo | Android 14 (API 34), mínimo API 24 |

## Capturas conceptuales del flujo

```
[ Login ] ─► [ Notas (lista dinámica) ] ─► [ Resultado: APROBADO / REPROBADO ]
   │
   └─► [ Registro ] ─┘
```

## Estructura del proyecto

```
app/src/main/java/com/oscar/notasapp/
├── MainActivity.kt          # Punto de entrada
├── data/                    # Capa de persistencia (Room)
│   ├── User.kt              # Entidad usuario
│   ├── Materia.kt           # Entidad materia/nota
│   ├── UserDao.kt
│   ├── MateriaDao.kt
│   └── AppDatabase.kt
├── ui/
│   ├── login/               # Login + Registro + AuthViewModel
│   ├── notas/               # NotasScreen + NotasViewModel
│   └── resultado/           # ResultadoScreen
├── navigation/
│   └── AppNavigation.kt     # NavHost y rutas
└── theme/                   # Color, Type, Theme
```

## Cómo ejecutar el proyecto

### Opción A · Clonar y abrir directamente

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/<tu-usuario>/NotasApp.git
   cd NotasApp
   ```
2. Abrir la carpeta en **Android Studio Hedgehog** (o superior).
3. Esperar a que Gradle sincronice las dependencias.
4. Ejecutar en un emulador o dispositivo físico (API 24+).

### Opción B · Importar el código en un proyecto nuevo

Si encuentras problemas con la sincronización de Gradle, puedes:

1. Crear un nuevo proyecto en Android Studio: **New Project → Empty Activity (Compose) → Kotlin**.
   - Package name: `com.oscar.notasapp`
   - Minimum SDK: **API 24**
2. Reemplazar los archivos generados por los de este repositorio (carpetas `app/src/main` y archivos Gradle).
3. Sincronizar Gradle y ejecutar.

## Reglas académicas implementadas

| Concepto | Valor |
| --- | --- |
| Escala | 0 – 10 |
| Nota mínima aprobatoria | 6.0 |
| Cálculo del promedio | media aritmética simple |

Los valores son constantes públicas en `NotasViewModel` y pueden modificarse fácilmente.

## Subir tu copia a GitHub (paso a paso)

1. Crear un repositorio nuevo en GitHub (sin README, sin .gitignore — ya están aquí).
2. Desde la carpeta del proyecto:
   ```bash
   git init
   git add .
   git commit -m "Primer commit: NotasApp con Jetpack Compose"
   git branch -M main
   git remote add origin https://github.com/<tu-usuario>/NotasApp.git
   git push -u origin main
   ```

## Documentación adjunta

- 📄 `Investigacion-JetpackCompose.pdf` — Investigación completa sobre Jetpack Compose.
- 🎥 Enlace al video de defensa (ver descripción del repositorio).

## Autor

**Oscar Quintanilla** — proyecto académico de Aplicaciones Móviles, 2026.
