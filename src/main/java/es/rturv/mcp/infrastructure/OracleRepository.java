package es.rturv.mcp.infrastructure;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OracleRepository {

    @Inject
    AgroalDataSource dataSource;

    public void ping() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
            if (!rs.next()) throw new SQLException("No se pudo ejecutar el ping");
        }
    }

    public RawQueryResult executeQuery(String sql, String schema) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (schema != null && !schema.isBlank()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION SET CURRENT_SCHEMA = " + schema);
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return mapResultSet(rs);
            }
        }
    }

    public String fetchDdl(String objectType, String objectName, String owner) throws SQLException {
        String sql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) AS DDL FROM DUAL";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, objectType.toUpperCase());
            stmt.setString(2, objectName.toUpperCase());
            if (owner != null && !owner.isBlank()) {
                stmt.setString(3, owner.toUpperCase());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DDL");
                }
                return null;
            }
        }
    }

    private RawQueryResult mapResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columns.add(meta.getColumnName(i));
        }

        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object val = rs.getObject(i);
                row.add(val == null ? "NULL" : val.toString());
            }
            rows.add(row);
        }
        return new RawQueryResult(columns, rows);
    }

    public record RawQueryResult(List<String> columns, List<List<String>> rows) {}
}
