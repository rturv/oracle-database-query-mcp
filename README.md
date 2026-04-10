# oracle-database-query-mcp

Servidor MCP (**Model Context Protocol**) de solo lectura para bases de datos **Oracle**, construido con [Quarkus](https://quarkus.io/) y ejecutado en modo **stdio**. Permite a clientes MCP como GitHub Copilot o Claude Desktop consultar una base de datos Oracle mediante lenguaje natural.

## Índice

- [Herramientas disponibles](#herramientas-disponibles-tools)
- [Variables de entorno](#variables-de-entorno)
- [Instalación desde Release](#instalación-desde-release-recomendado)
- [Compilar desde fuente](#compilar-desde-fuente)
- [Ejecutar el servidor](#ejecutar-el-servidor)
- [Configurar en Copilot / Claude Desktop](#configurar-en-copilot--claude-desktop)
- [Desarrollo local](#desarrollo-local)

---

## Herramientas disponibles (Tools)

| Tool | Descripción |
|---|---|
| `ping` | Prueba la conexión JDBC |
| `query` | Ejecuta un `SELECT` o `WITH`. Acepta esquema opcional para la sesión |
| `describeSchema` | Lista objetos (tablas, vistas, funciones…) de un esquema |
| `describeTable` | Describe columnas, tipos y propiedades de una tabla |
| `ddl` | Obtiene el DDL de un objeto vía `DBMS_METADATA.GET_DDL` |
| `listTables` | Lista rápida de tablas por propietario |
| `sessionInfo` | Muestra usuario, esquema y base de datos de la sesión activa |

---

## Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `JDBC_URL` | URL de conexión Oracle | `jdbc:oracle:thin:@localhost:1521/ORCLPDB1` |
| `JDBC_USER` | Usuario de la base de datos | `mcp_user` |
| `JDBC_PASSWORD` | Contraseña | `secret` |
| `ORACLE_CHARSET` | Juego de caracteres (por defecto `UTF-8`) | `UTF-8` |

---

## Instalación desde Release (recomendado)

Descarga los artefactos precompilados desde la [página de Releases](https://github.com/rturv/oracle-database-query-mcp/releases).

### Opción A — ZIP JVM (Windows / Mac / Linux con Java 21)

```bash
# Descomprimir
unzip oracle-database-query-mcp-1.0.1-jvm.zip

# Ejecutar
JDBC_URL=jdbc:oracle:thin:@host:1521/service \
JDBC_USER=usuario \
JDBC_PASSWORD=contraseña \
java -jar target/quarkus-app/quarkus-run.jar
```

> Requiere Java 21 instalado.

### Opción B — Binario nativo Linux (sin Java)

```bash
# Dar permisos de ejecución
chmod +x oracle-database-query-mcp-1.0.1-linux-x86_64

# Ejecutar directamente
JDBC_URL=jdbc:oracle:thin:@host:1521/service \
JDBC_USER=usuario \
JDBC_PASSWORD=contraseña \
./oracle-database-query-mcp-1.0.1-linux-x86_64
```

> No requiere JVM. Arranca en milisegundos.

### Opción C — Imagen Docker (nativa)

```bash
docker pull rturv/oracle-database-query-mcp:1.0.1-native

docker run -i --rm \
  -e JDBC_URL=jdbc:oracle:thin:@host:1521/ORCLPDB1 \
  -e JDBC_USER=usuario \
  -e JDBC_PASSWORD=contraseña \
  rturv/oracle-database-query-mcp:1.0.1-native
```


---

## Compilar desde fuente

Requisitos: Java 21, Maven (o usa el wrapper `./mvnw`).

### Build JVM (estándar)

```bash
./mvnw package -DskipTests
# Artefacto: target/quarkus-app/quarkus-run.jar
```

### Build nativo con GraalVM local

```bash
./mvnw package -Dnative -DskipTests
# Artefacto: target/*-runner
```

### Build nativo con Docker (sin GraalVM instalado)

```bash
./mvnw clean package -Dnative -DskipTests \
  -Dquarkus.native.container-build=true \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.image=rturv/oracle-database-query-mcp:x.y.z-native \
  -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native
# Artefacto: target/*-runner  +  imagen Docker local
```

> Solo requiere Docker instalado, no GraalVM.

---

## Ejecutar el servidor

El servidor usa **stdio** como transporte MCP: los clientes lo lanzan como proceso hijo y se comunican por stdin/stdout.

```bash
# JAR JVM
java -jar target/quarkus-app/quarkus-run.jar

# Binario nativo
./target/oracle-database-query-mcp-x.y.z-runner
```

### Probar con el Inspector MCP (interfaz web)

```bash
# PowerShell
$env:JDBC_URL="jdbc:oracle:thin:@localhost:1521/XE"
$env:JDBC_USER="user"
$env:JDBC_PASSWORD="pass"
npx @modelcontextprotocol/inspector java -jar target/quarkus-app/quarkus-run.jar
```

---

## Configurar en Copilot / Claude Desktop

Consulta [COPILOT_CONFIG.md](COPILOT_CONFIG.md) para instrucciones detalladas de configuración en VS Code (GitHub Copilot) y Claude Desktop.

---

## Desarrollo local

```bash
# Modo live-coding (Quarkus Dev)
./mvnw quarkus:dev

# Tests
./mvnw test

# Build sin tests
./mvnw package -DskipTests
```

> La Dev UI está disponible en modo dev en `http://localhost:8080/q/dev/`.

### Arquitectura

El proyecto sigue un patrón de arquitectura hexagonal (lite):

- **API (Fachada MCP):** `OracleMcpServer` — definición de tools y entrada del protocolo.
- **Service (Negocio):** `OracleService` — validación de solo lectura y orquestación.
- **Infrastructure (Persistencia):** `OracleRepository` — acceso JDBC via Agroal.
