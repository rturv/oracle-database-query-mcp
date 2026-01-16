package es.rturv.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
class OracleMcpServerTest {

    @Inject
    OracleMcpServer mcpServer;

    @InjectMock
    AgroalDataSource dataSource;

    @Test
    void testPingSuccess() throws SQLException {
        Connection conn = Mockito.mock(Connection.class);
        Statement stmt = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);

        Mockito.when(dataSource.getConnection()).thenReturn(conn);
        Mockito.when(conn.createStatement()).thenReturn(stmt);
        Mockito.when(stmt.executeQuery("SELECT 1 FROM DUAL")).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true);

        ToolResponse response = mcpServer.ping();

        assertNotNull(response);
        assertFalse(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("¡Pong!"));
    }

    @Test
    void testQuerySelectionValidation() {
        // Test that non-SELECT queries are rejected
        ToolResponse response = mcpServer.query("DROP TABLE USERS", Optional.empty());

        assertNotNull(response);
        assertTrue(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("Solo Lectura"));
    }

    @Test
    void testDescribeTableSuccess() throws SQLException {
        Connection conn = Mockito.mock(Connection.class);
        Statement stmt = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        ResultSetMetaData meta = Mockito.mock(ResultSetMetaData.class);

        Mockito.when(dataSource.getConnection()).thenReturn(conn);
        Mockito.when(conn.createStatement()).thenReturn(stmt);
        Mockito.when(stmt.executeQuery(anyString())).thenReturn(rs);
        Mockito.when(rs.getMetaData()).thenReturn(meta);

        // Mock meta
        Mockito.when(meta.getColumnCount()).thenReturn(2);
        Mockito.when(meta.getColumnName(1)).thenReturn("COLUMN_NAME");
        Mockito.when(meta.getColumnName(2)).thenReturn("DATA_TYPE");

        // Mock rows
        Mockito.when(rs.next()).thenReturn(true, false);
        Mockito.when(rs.getObject(1)).thenReturn("ID");
        Mockito.when(rs.getObject(2)).thenReturn("NUMBER");

        ToolResponse response = mcpServer.describeTable("MY_TABLE", Optional.empty());

        assertNotNull(response);
        assertFalse(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("COLUMN_NAME"));
        assertTrue(content.text().contains("ID"));
    }

    @Test
    void testDdlWithNullSchema() throws SQLException {
        Connection conn = Mockito.mock(Connection.class);
        PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);

        Mockito.when(dataSource.getConnection()).thenReturn(conn);
        Mockito.when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        Mockito.when(pstmt.executeQuery()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(true);
        Mockito.when(rs.getString("DDL")).thenReturn("CREATE TABLE TEST...");

        ToolResponse response = mcpServer.ddl("TABLE", "MY_TABLE", Optional.empty());

        assertNotNull(response);
        assertFalse(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("CREATE TABLE"));

        // Verificamos que se llamó a setNull para el tercer parámetro (owner)
        Mockito.verify(pstmt).setNull(Mockito.eq(3), Mockito.eq(Types.VARCHAR));
    }
}
