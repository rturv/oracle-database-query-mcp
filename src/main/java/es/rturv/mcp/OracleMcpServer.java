package es.rturv.mcp;

import es.rturv.mcp.service.OracleService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Fachada de herramientas MCP para Oracle Database.
 * Esta clase delega la lógica de negocio y acceso a datos a la capa de servicio.
 */
@ApplicationScoped
public class OracleMcpServer {

    @Inject
    OracleService oracleService;

    @Tool(description = "Prueba la conexión con la base de datos Oracle")
    public ToolResponse ping() {
        return oracleService.ping();
    }

    @Tool(description = "Ejecuta una consulta SQL de solo lectura")
    public ToolResponse query(
            @ToolArg(description = "La sentencia SQL SELECT a ejecutar") String sql,
            @ToolArg(description = "Esquema opcional para la sesión") Optional<String> schema) {
        return oracleService.query(sql, schema);
    }

    @Tool(description = "Describe el esquema de base de datos (tablas, vistas, triggers, etc.)")
    public ToolResponse describeSchema(
            @ToolArg(description = "Nombre del esquema (opcional, por defecto el usuario actual)") Optional<String> schema) {
        return oracleService.describeSchema(schema);
    }

    @Tool(description = "Describe las columnas y propiedades de una tabla específica")
    public ToolResponse describeTable(
            @ToolArg(description = "Nombre de la tabla") String tableName,
            @ToolArg(description = "Esquema (opcional)") Optional<String> schema) {
        return oracleService.describeTable(tableName, schema);
    }

    @Tool(description = "Obtiene el DDL (Data Definition Language) de un objeto (TABLE, VIEW, TRIGGER, etc.)")
    public ToolResponse ddl(
            @ToolArg(description = "Tipo de objeto (TABLE, VIEW, TRIGGER, etc.)") String objectType,
            @ToolArg(description = "Nombre del objeto") String objectName,
            @ToolArg(description = "Esquema (opcional)") Optional<String> schema) {
        return oracleService.getDdl(objectType, objectName, schema);
    }

    @Tool(description = "Lista las tablas disponibles en el esquema")
    public ToolResponse listTables(
            @ToolArg(description = "Esquema (opcional)") Optional<String> schema) {
        return oracleService.listTables(schema);
    }

    @Tool(description = "Muestra información de la sesión actual (usuario, esquema, base de datos)")
    public ToolResponse sessionInfo() {
        return oracleService.getSessionInfo();
    }
}
