package es.rturv.mcp.service;

import es.rturv.mcp.infrastructure.OracleRepository;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OracleService {

    private static final Logger LOG = Logger.getLogger(OracleService.class);

    @Inject
    OracleRepository repository;

    public ToolResponse ping() {
        try {
            repository.ping();
            return ToolResponse.success(new TextContent("¡Pong! Conexión JDBC establecida correctamente."));
        } catch (SQLException e) {
            LOG.error("Error en ping", e);
            return ToolResponse.error("Fallo de conexión: " + e.getMessage());
        }
    }

    public ToolResponse query(String sql, Optional<String> schema) {
        String trimmedSql = sql.trim();
        String upperSql = trimmedSql.toUpperCase();
        
        if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("WITH")) {
            return ToolResponse.error("Modo Solo Lectura: Solo se permiten consultas SELECT o WITH.");
        }

        try {
            OracleRepository.RawQueryResult result = repository.executeQuery(trimmedSql, schema.orElse(null));
            return formatAsMarkdownTable(result);
        } catch (SQLException e) {
            LOG.error("Error ejecutando query: " + trimmedSql, e);
            return ToolResponse.error("Error SQL: " + e.getMessage());
        }
    }

    public ToolResponse describeSchema(Optional<String> schema) {
        String owner = schema.map(String::toUpperCase).orElse("USER");
        String sql = "SELECT object_type, object_name, status FROM ALL_OBJECTS " +
                     "WHERE OWNER = " + (owner.equals("USER") ? "USER" : "'" + owner + "'") + " " +
                     "AND OBJECT_TYPE IN ('TABLE', 'VIEW', 'TRIGGER', 'PROCEDURE', 'FUNCTION', 'PACKAGE') " +
                     "ORDER BY OBJECT_TYPE, OBJECT_NAME";
        return executeAndFormat(sql);
    }

    public ToolResponse describeTable(String tableName, Optional<String> schema) {
        String sql = "SELECT column_name, data_type, data_length, nullable, data_default " +
                     "FROM ALL_TAB_COLUMNS " +
                     "WHERE TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                     (schema.isPresent() ? "AND OWNER = '" + schema.get().toUpperCase() + "' " : "AND OWNER = USER ") +
                     "ORDER BY COLUMN_ID";
        return executeAndFormat(sql);
    }

    public ToolResponse describeConstraints(String tableName, Optional<String> schema) {
        String ownerCond = schema.isPresent() ? "AND ac.owner = '" + schema.get().toUpperCase() + "' " : "AND ac.owner = USER ";

        String sql = "SELECT ac.constraint_name AS constraint_name, " +
                     "ac.constraint_type AS constraint_type, " +
                     "LISTAGG(acc.column_name, ',') WITHIN GROUP (ORDER BY acc.position) AS columns, " +
                     "rc.owner AS r_owner, rc.table_name AS r_table_name, " +
                     "LISTAGG(acc.column_name || '->' || racc.column_name, ',') WITHIN GROUP (ORDER BY acc.position) AS column_mapping " +
                     "FROM ALL_CONSTRAINTS ac " +
                     "JOIN ALL_CONS_COLUMNS acc ON ac.owner = acc.owner AND ac.constraint_name = acc.constraint_name " +
                     "LEFT JOIN ALL_CONSTRAINTS rc ON ac.r_owner = rc.owner AND ac.r_constraint_name = rc.constraint_name " +
                     "LEFT JOIN ALL_CONS_COLUMNS racc ON rc.owner = racc.owner AND rc.constraint_name = racc.constraint_name AND acc.position = racc.position " +
                     "WHERE ac.table_name = '" + tableName.toUpperCase() + "' " + ownerCond +
                     "GROUP BY ac.constraint_name, ac.constraint_type, rc.owner, rc.table_name " +
                     "ORDER BY ac.constraint_name";

        try {
            OracleRepository.RawQueryResult result = repository.executeQuery(sql, null);
            return formatAsMarkdownTable(result);
        } catch (SQLException e) {
            LOG.error("Error obteniendo constraints para tabla " + tableName, e);
            return ToolResponse.error("Error SQL: " + e.getMessage());
        }
    }

    public ToolResponse getDdl(String objectType, String objectName, Optional<String> schema) {
        String owner = schema.map(String::toUpperCase).orElse(null);
        try {
            String ddl = repository.fetchDdl(objectType, objectName, owner);
            if (ddl != null) {
                return ToolResponse.success(new TextContent(ddl));
            }
            return ToolResponse.error("Objeto no encontrado o DDL no disponible.");
        } catch (SQLException e) {
            LOG.error("Error obteniendo DDL", e);
            return ToolResponse.error("Error al obtener DDL: " + e.getMessage());
        }
    }

    public ToolResponse listTables(Optional<String> schema) {
        String owner = schema.map(s -> "'" + s.toUpperCase() + "'").orElse("USER");
        String sql = "SELECT table_name FROM ALL_TABLES WHERE OWNER = " + owner + " ORDER BY table_name";
        return executeAndFormat(sql);
    }

    public ToolResponse getSessionInfo() {
        String sql = "SELECT USER AS CURRENT_USER, SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS CURRENT_SCHEMA, " +
                     "SYS_CONTEXT('USERENV', 'DB_NAME') AS DATABASE_NAME FROM DUAL";
        return executeAndFormat(sql);
    }

    private ToolResponse executeAndFormat(String sql) {
        try {
            OracleRepository.RawQueryResult result = repository.executeQuery(sql, null);
            return formatAsMarkdownTable(result);
        } catch (SQLException e) {
            return ToolResponse.error("Error SQL: " + e.getMessage());
        }
    }

    private ToolResponse formatAsMarkdownTable(OracleRepository.RawQueryResult result) {
        if (result.rows().isEmpty()) {
            return ToolResponse.success(new TextContent("No se encontraron resultados."));
        }

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("| ").append(String.join(" | ", result.columns())).append(" |\n");
        // Separator
        sb.append("| ").append(result.columns().stream().map(c -> "---").collect(Collectors.joining(" | "))).append(" |\n");
        // Rows
        for (List<String> row : result.rows()) {
            sb.append("| ").append(row.stream().map(v -> v.replace("\n", " ")).collect(Collectors.joining(" | "))).append(" |\n");
        }

        return ToolResponse.success(new TextContent(sb.toString()));
    }
}
