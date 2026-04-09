package es.rturv.mcp.service;

import es.rturv.mcp.infrastructure.OracleRepository;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class OracleServiceTest {

    @Test
    void describeTable_emptyResult_returnsNoResults() throws SQLException {
        OracleService service = new OracleService();
        OracleRepository repo = mock(OracleRepository.class);
        service.repository = repo; // package-private injection

        List<String> columns = List.of("C1");
        List<List<String>> rows = List.of();
        OracleRepository.RawQueryResult result = new OracleRepository.RawQueryResult(columns, rows);

        when(repo.executeQuery(anyString(), isNull())).thenReturn(result);

        ToolResponse response = service.describeTable("T", Optional.empty());
        assertNotNull(response);
        assertFalse(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("No se encontraron resultados."));
        verify(repo).executeQuery(startsWith("SELECT"), isNull());
    }

    @Test
    void describeSchema_buildsSql_withAndWithoutOwner() throws SQLException {
        OracleService service = new OracleService();
        OracleRepository repo = mock(OracleRepository.class);
        service.repository = repo;

        List<String> columns = List.of("C1");
        List<List<String>> rows = List.of();
        OracleRepository.RawQueryResult result = new OracleRepository.RawQueryResult(columns, rows);

        when(repo.executeQuery(anyString(), isNull())).thenReturn(result);

        // call with schema
        service.describeSchema(Optional.of("MYSCHEMA"));
        // call without schema
        service.describeSchema(Optional.empty());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repo, times(2)).executeQuery(captor.capture(), isNull());
        var vals = captor.getAllValues();
        assertTrue(vals.get(0).contains("WHERE OWNER = 'MYSCHEMA'"));
        assertTrue(vals.get(1).contains("WHERE OWNER = USER"));
    }

    @Test
    void query_acceptsWithPrefix_and_formatsResult() throws SQLException {
        OracleService service = new OracleService();
        OracleRepository repo = mock(OracleRepository.class);
        service.repository = repo;

        List<String> columns = List.of("C1");
        List<List<String>> rows = List.of(List.of("v1\nv2"));
        OracleRepository.RawQueryResult result = new OracleRepository.RawQueryResult(columns, rows);

        when(repo.executeQuery(anyString(), isNull())).thenReturn(result);

        String sql = "WITH t AS (SELECT 1 FROM DUAL) SELECT * FROM t";
        var response = service.query(sql, Optional.empty());
        assertNotNull(response);
        assertFalse(response.isError());
        TextContent content = (TextContent) response.content().get(0);
        assertTrue(content.text().contains("| C1 |"));
        assertTrue(content.text().contains("v1 v2"));
    }
}
