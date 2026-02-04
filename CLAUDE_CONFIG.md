# Configuración de Oracle Database Query MCP para Claude Desktop

Este documento detalla cómo integrar este servidor MCP en la aplicación Claude Desktop.

## Archivo de Configuración

Claude Desktop utiliza un archivo `claude_desktop_config.json` para gestionar sus servidores MCP. Dependiendo de tu sistema operativo, se encuentra en:

- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`

## Modos de Instalación

### 1. Modo Local (stdio) - Desarrollo y Pruebas iniciales

Para usar el código que tienes actualmente en local:

1. **Construir el proyecto:**
   Abre una terminal en la raíz del proyecto y ejecuta:
   ```bash
   ./mvnw package
   ```
   Esto generará el directorio `target/quarkus-app/`.

2. **Editar la configuración de Claude:**
   Añade la siguiente entrada en la sección `"mcpServers"`:

   ```json
   {
     "mcpServers": {
       "oracle-db-local": {
         "command": "java",
         "args": [
           "-jar",
           "C:/Proyectos/2025/IA/mio/oracle-database-query-mcp/target/quarkus-app/quarkus-run.jar"
         ],
         "env": {
           "JDBC_URL": "<cadena jdbc del recurso oracle>", // ejemplo: "jdbc:oracle:thin:@localhost:1521/ORCLPDB1",
           "JDBC_USER": "mi_usuario",
           "JDBC_PASSWORD": "mi_password",
           "PATH": "<path a aplicar para ejecutar comando java>", //opcional pero conveniente
		       "JAVA_HOME": "<Ruta donde esta instalado el JRE/JDK que quieres aplicar>" //opcional pero conveniente
         }
       }
     }
   }
   ```
   *Nota: Asegúrate de que `java` esté en tu PATH o usa la ruta absoluta al ejecutable (ej: `C:/Program Files/Java/jdk-21/bin/java.exe`).*

### 2. Modo Docker (Recomendado para uso final)

Una vez que la imagen esté publicada en Docker Hub bajo el usuario `rturv`:

```json
{
  "mcpServers": {
    "oracle-db": {
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

## Notas Adicionales

- **JDBC_URL:** Al usar una URL JDBC completa, tienes más flexibilidad para incluir parámetros adicionales de conexión (ej: TNS names, carteras, etc.).
- **Logs:** Si necesitas depurar la conexión, puedes revisar los logs de Claude Desktop o cambiar el nivel de log en `application.properties` a `DEBUG`.
- **Conectividad:** Si el servidor MCP corre en Docker y la base de datos en tu máquina host (fuera de Docker), asegúrate de que la `JDBC_URL` use `host.docker.internal` en lugar de `localhost`.
- **Reinicio:** Cada vez que modifiques el archivo `claude_desktop_config.json`, debes **reiniciar Claude Desktop por completo** (cerrar desde la bandeja de sistema y volver a abrir).
