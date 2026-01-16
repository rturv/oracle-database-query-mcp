# Configuración de Oracle Database Query MCP para GitHub Copilot (VS Code)

Este documento explica cómo configurar este servidor MCP en VS Code para usarlo con GitHub Copilot.

## Requisitos Previos

1. Tener instalado [VS Code](https://code.visualstudio.com/).
2. Tener la extensión [GitHub Copilot](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot) activa.
3. (Opcional) Tener Docker instalado si prefieres usar la imagen del contenedor.

## Configuración en VS Code

GitHub Copilot admite servidores MCP a través de su configuración de extensiones o mediante el archivo de configuración de Claude Desktop (que Copilot puede importar).

### Opción 1: Modo Local (stdio) - Para Desarrollo/Pruebas

Esta es la mejor opción para probar cambios locales antes de publicar la imagen.

1. **Compilar el proyecto:**
   ```bash
   ./mvnw package
   ```

2. **Configurar las variables de entorno:**
   Asegúrate de tener acceso a tu base de datos Oracle. Las variables necesarias son:
   - `JDBC_URL`: URL de conexión completa (ej: `jdbc:oracle:thin:@localhost:1521/ORCLPDB1`)
   - `JDBC_USER`: Usuario
   - `JDBC_PASSWORD`: Contraseña

3. **Añadir a la configuración de Copilot:**
   En VS Code, abre el Command Palette (`Ctrl+Shift+P`) y busca "Copilot: Configure MCP Servers".
   O añade manualmente al archivo de configuración de MCP:

   ```json
   {
     "mcpServers": {
       "oracle-mcp-local": {
         "command": "java",
         "args": [
           "-jar",
           "C:/Proyectos/2025/IA/mio/oracle-database-query-mcp/target/quarkus-app/quarkus-run.jar"
         ],
         "env": {
           "JDBC_URL": "jdbc:oracle:thin:@tu_host:1521/XE",
           "JDBC_USER": "mi_usuario",
           "JDBC_PASSWORD": "mi_password"
         }
       }
     }
   }
   ```
   *Nota: Se recomienda usar la ruta absoluta tanto para el ejecutable de `java` como para el `JAVA_HOME` para evitar conflictos de versiones.*

### Opción 2: Modo Docker (Producción/Publicado)

Cuando el contenedor esté disponible en Docker Hub (`rturv/oracle-database-query-mcp`):

```json
{
  "mcpServers": {
    "oracle-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "JDBC_URL=oracle:thin:@host.docker.internal:1521/XE",
        "-e", "JDBC_USER=mi_usuario",
        "-e", "JDBC_PASSWORD=mi_password",
        "rturv/oracle-database-query-mcp:latest"
      ]
    }
  }
}
```

## Uso en el Chat

Una vez configurado, puedes preguntar a Copilot cosas como:
- "@mcp ¿qué tablas hay en el esquema HR?"
- "@mcp ejecuta una consulta para ver los últimos 10 pedidos"
- "@mcp describe la estructura de la tabla EMPLOYEES"
