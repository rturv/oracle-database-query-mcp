package es.rturv.mcp.infrastructure;

import io.agroal.api.AgroalDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleRepositoryTest {

    @Test
    void fetchDdl_withOwner_callsSetString3_andReturnsDdl() throws Exception {
        OracleRepository repo = new OracleRepository();
        AgroalDataSource ds = mock(AgroalDataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(conn);
        String expectedSql = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) AS DDL FROM DUAL";
        when(conn.prepareStatement(expectedSql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("DDL")).thenReturn("CREATE TABLE TEST...");

        repo.dataSource = ds; // package-private injection

        String ddl = repo.fetchDdl("TABLE", "MYTABLE", "OWNER");
        assertEquals("CREATE TABLE TEST...", ddl);
        verify(pstmt).setString(3, "OWNER");
    }

    @Test
    void executeQuery_withSchema_executesAlterSession_andMapsResult() throws Exception {
        OracleRepository repo = new OracleRepository();
        AgroalDataSource ds = mock(AgroalDataSource.class);
        Connection conn = mock(Connection.class);
        Statement stmt1 = mock(Statement.class);
        Statement stmt2 = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt1, stmt2);

        String sql = "SELECT 1 FROM DUAL";
        when(stmt1.execute("ALTER SESSION SET CURRENT_SCHEMA = SCHEMA")).thenReturn(true);
        when(stmt2.executeQuery(sql)).thenReturn(rs);

        when(rs.getMetaData()).thenReturn(meta);
        when(meta.getColumnCount()).thenReturn(1);
        when(meta.getColumnName(1)).thenReturn("C1");
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(1)).thenReturn("V1");

        repo.dataSource = ds;

        OracleRepository.RawQueryResult result = repo.executeQuery(sql, "SCHEMA");
        assertEquals(1, result.columns().size());
        assertEquals("C1", result.columns().get(0));
        assertEquals(1, result.rows().size());
        assertEquals("V1", result.rows().get(0).get(0));
        verify(stmt1).execute("ALTER SESSION SET CURRENT_SCHEMA = SCHEMA");
    }
}
