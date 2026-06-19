# CacaoScan - Módulo de Autenticación (Backend)

Este repositorio contiene el backend del módulo síncrono de autenticación y registro de agricultores para la aplicación móvil **CacaoScan AI Vision**. Está construido sobre Spring Boot 3.x, Spring Security, JWT, PostgreSQL, Redis y Flyway.

---

## 🛠️ Tecnologías y Stack

* **Java 17** y **Spring Boot 3.3.x**
* **Spring Security + JWT** (Access Token de 15 min y Refresh Token persistido en BD)
* **PostgreSQL** para la persistencia relacional de usuarios y tokens.
* **Redis** para el control de intentos fallidos (Rate Limiting), blacklist de tokens y tokens temporales de recuperación.
* **Maildev** como servidor SMTP de prueba local para capturar correos de restablecimiento de contraseña.
* **Flyway** para migraciones de bases de datos versionadas.
* **Springdoc OpenAPI** para la documentación interactiva de endpoints.

---

## 📋 Requisitos Previos

Asegúrate de tener instalados los siguientes componentes:
* **Java 17 (JDK)**
* **Maven** (o usa el wrapper `./mvnw` incluido)
* **Docker y Docker Compose**

---

## 🚀 Guía de Configuración Local (Menos de 10 Minutos)

### 1. Iniciar Servicios Externos (Base de Datos, Redis, Maildev)
Navega a la carpeta raíz y levanta la infraestructura local con Docker Compose:

```bash
docker compose up -d
```

Esto levantará:
* **PostgreSQL:** en `localhost:5432` (Credenciales: `cacaouser` / `cacaopassword`)
* **Redis:** en `localhost:6379`
* **Maildev:** servidor SMTP en `localhost:1025` y consola web de correos en `http://localhost:1080`

### 2. Configurar Variables de Entorno
Copia el archivo `.env.example` a un archivo `.env` en la raíz del proyecto y ajusta las variables si es necesario:

```bash
cp .env.example .env
```

*Nota: Spring Boot cargará automáticamente estas variables del sistema. Si ejecutas desde un IDE, asegúrate de configurar estas variables en el entorno de ejecución.*

### 3. Compilar y Ejecutar la Aplicación
Ejecuta las migraciones y levanta el servidor web:

```bash
mvn spring-boot:run
# O usando el wrapper de maven en Linux/macOS:
./mvnw spring-boot:run
# O en Windows:
.\mvnw.cmd spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

---

## 📖 Documentación de la API (OpenAPI)

Una vez que la aplicación esté corriendo, puedes acceder a la documentación interactiva y probar los endpoints desde Swagger UI en:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### Resumen de Endpoints:

| Método | Endpoint | Descripción | Acceso |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/auth/register` | Registro de un nuevo agricultor | Público |
| **POST** | `/api/v1/auth/login` | Inicio de sesión, devuelve tokens y datos de usuario | Público |
| **POST** | `/api/v1/auth/refresh` | Renueva el Access Token usando un Refresh Token | Público |
| **POST** | `/api/v1/auth/forgot-password` | Envía correo local con token para restablecer clave | Público |
| **POST** | `/api/v1/auth/reset-password` | Cambia la contraseña con el token de recuperación | Público |

---

## 🛡️ Reglas de Negocio Implementadas

1. **Rate Limiting (Redis):** Máximo 5 intentos de inicio de sesión fallidos por email+IP en 15 minutos. Si se excede, la cuenta se bloquea temporalmente devolviendo un error `423 Locked` y un indicador `retryAfter` (tiempo en segundos para reintentar).
2. **Seguridad JWT:** Contraseñas hasheadas con BCrypt. Access Token de vida corta (15 min) y Refresh Token persistido de forma segura hasheado en PostgreSQL.
3. **Recuperación Segura:** El endpoint `/forgot-password` no revela si el email está registrado o no (seguridad contra enumeración). Genera un token UUID de uso único almacenado en Redis con TTL de 30 minutos.
4. **Invalidación de Sesión:** Tras cambiar con éxito la contraseña en `/reset-password`, se eliminan todos los refresh tokens activos del usuario, cerrando sesión en todos los dispositivos.
