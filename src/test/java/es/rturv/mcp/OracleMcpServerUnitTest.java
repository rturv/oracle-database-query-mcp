package es.rturv.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import es.rturv.mcp.service.OracleService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleMcpServerUnitTest {

    @Test
    void describeSchema_delegatesToOracleService() {
        OracleMcpServer server = new OracleMcpServer();
        OracleService service = mock(OracleService.class);

        ToolResponse fake = ToolResponse.success(new TextContent("OK"));
        when(service.describeSchema(Optional.of("SCHEMA"))).thenReturn(fake);
        when(service.describeSchema(Optional.empty())).thenReturn(fake);

        // inject mock (package-private field)
        server.oracleService = service;

        ToolResponse r1 = server.describeSchema(Optional.of("SCHEMA"));
        assertSame(fake, r1);
        verify(service).describeSchema(Optional.of("SCHEMA"));

        ToolResponse r2 = server.describeSchema(Optional.empty());
        assertSame(fake, r2);
        verify(service).describeSchema(Optional.empty());
    }
}
