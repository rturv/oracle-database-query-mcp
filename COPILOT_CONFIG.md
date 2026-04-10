# Configuración de Oracle Database Query MCP para GitHub Copilot y Claude Desktop

Este documento explica cómo conectar este servidor MCP a tu cliente IA preferido.

## Prerrequisitos

- Acceso a una base de datos Oracle con usuario de solo lectura.
- Una de las siguientes opciones de instalación del servidor:
  - **ZIP JVM**: Java 21 instalado + ZIP descargado de la [página de Releases](https://github.com/rturv/oracle-database-query-mcp/releases).
  - **Binario nativo Linux**: solo el binario descargado de Releases (sin Java).
  - **Docker**: Docker instalado.
  - **Compilado local**: Java 21 + Maven (ver [README.md](README.md)).

---

## Opciones de instalación del servidor

### Opción 1 — Binario nativo Linux (más sencilla en Linux)

Descarga `oracle-database-query-mcp-X.Y.Z-linux-x86_64` de la [página de Releases](https://github.com/rturv/oracle-database-query-mcp/releases) y dale permisos de ejecución:

```bash
chmod +x oracle-database-query-mcp-1.0.1-linux-x86_64
```

### Opción 2 — ZIP JVM (Windows / Mac / Linux)

Descarga y descomprime `oracle-database-query-mcp-X.Y.Z-jvm.zip` de Releases. El JAR estará en `target/quarkus-app/quarkus-run.jar`.

### Opción 3 — Docker (cualquier plataforma)

```bash
docker pull rturv/oracle-database-query-mcp:1.0.1-native
```

### Opción 4 — Compilado local

```bash
git clone https://github.com/rturv/oracle-database-query-mcp.git
cd oracle-database-query-mcp
./mvnw package -DskipTests
# JAR disponible en target/quarkus-app/quarkus-run.jar
```

---

## Variables de entorno necesarias

| Variable | Descripción | Ejemplo |
|---|---|---|
| `JDBC_URL` | URL de conexión Oracle | `jdbc:oracle:thin:@localhost:1521/ORCLPDB1` |
| `JDBC_USER` | Usuario de la BD | `mcp_user` |
| `JDBC_PASSWORD` | Contraseña | `secret` |
| `ORACLE_CHARSET` | Juego de caracteres (opcional, defecto `UTF-8`) | `UTF-8` |

---

## Configuración en VS Code (GitHub Copilot)

Abre la paleta de comandos (`Ctrl+Shift+P`) → **"MCP: Add Server"**, o edita manualmente el archivo de configuración MCP de VS Code (normalmente en `.vscode/mcp.json` o en la configuración de usuario).

### Con ZIP JVM (Windows)

```json
{
  "servers": {
    "oracle-mcp": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "C:/ruta/a/target/quarkus-app/quarkus-run.jar"
      ],
      "env": {
        "JDBC_URL": "jdbc:oracle:thin:@localhost:1521/ORCLPDB1",
        "JDBC_USER": "mcp_user",
        "JDBC_PASSWORD": "mi_password",
        "ORACLE_CHARSET": "UTF-8",
        "JAVA_HOME": "C:/programas/jdk-21.0.10"
      }
    }
  }
}
```

> Usa `JAVA_HOME` para fijar la versión de Java si tienes varias instaladas.

### Con binario nativo (Linux / Mac)

```json
{
  "servers": {
    "oracle-mcp": {
      "type": "stdio",
      "command": "/ruta/a/oracle-database-query-mcp-1.0.1-linux-x86_64",
      "args": [],
      "env": {
        "JDBC_URL": "jdbc:oracle:thin:@localhost:1521/ORCLPDB1",
        "JDBC_USER": "mcp_user",
        "JDBC_PASSWORD": "mi_password"
      }
    }
  }
}
```

### Con Docker

```json
{
  "servers": {
    "oracle-mcp": {
      "type": "stdio",
      "command": "docker",
      "args": [
        "run", "-i", "--rm",
        "-e", "JDBC_URL=jdbc:oracle:thin:@host.docker.internal:1521/ORCLPDB1",
        "-e", "JDBC_USER=mcp_user",
        "-e", "JDBC_PASSWORD=mi_password",
        "-e", "ORACLE_CHARSET=UTF-8",
        "rturv/oracle-database-query-mcp:1.0.1-native"
      ]
    }
  }
}
```

---

## Configuración en Claude Desktop

Edita el archivo de configuración de Claude Desktop:
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
- **Mac:** `~/Library/Application Support/Claude/claude_desktop_config.json`

### Con ZIP JVM (Windows)

```json
{
  "mcpServers": {
    "oracle-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "C:/ruta/a/target/quarkus-app/quarkus-run.jar"
      ],
      "env": {
        "JDBC_URL": "jdbc:oracle:thin:@localhost:1521/ORCLPDB1",
        "JDBC_USER": "mcp_user",
        "JDBC_PASSWORD": "mi_password",
        "ORACLE_CHARSET": "UTF-8",
        "JAVA_HOME": "C:/programas/jdk-21.0.10"
      }
    }
  }
}
```

### Con binario nativo (Linux / Mac)

```json
{
  "mcpServers": {
    "oracle-mcp": {
      "command": "/ruta/a/oracle-database-query-mcp-1.0.1-linux-x86_64",
      "args": [],
      "env": {
        "JDBC_URL": "jdbc:oracle:thin:@localhost:1521/ORCLPDB1",
        "JDBC_USER": "mcp_user",
        "JDBC_PASSWORD": "mi_password"
      }
    }
  }
}
```

### Con Docker

```json
{
  "mcpServers": {
    "oracle-mcp": {
      "command": "docker",
      "args": [
        "run", "-i", "--rm",
        "-e", "JDBC_URL=jdbc:oracle:thin:@host.docker.internal:1521/ORCLPDB1",
        "-e", "JDBC_USER=mcp_user",
        "-e", "JDBC_PASSWORD=mi_password",
        "-e", "ORACLE_CHARSET=UTF-8",
        "rturv/oracle-database-query-mcp:1.0.1-native"
      ]
    }
  }
}
```

---

## Uso en el chat

Una vez configurado, puedes preguntar cosas como:

- *"¿Qué tablas hay en el esquema HR?"*
- *"Muestra los últimos 10 registros de la tabla EMPLOYEES"*
- *"Describe la estructura de la tabla ORDERS"*
- *"¿Cuál es el DDL de la tabla CUSTOMERS?"*
- *"¿Estoy conectado a la base de datos correcta?"* (usa `sessionInfo`)
