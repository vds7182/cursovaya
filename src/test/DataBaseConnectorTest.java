package org.example;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {

    @Test
    void testGetConnection() {
        assertDoesNotThrow(() -> {
            Connection conn = DatabaseConnector.getConnection();
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            DatabaseConnector.closeConnection(conn);
        });
    }

    @Test
    void testCloseConnection() {
        assertDoesNotThrow(() -> {
            Connection conn = DatabaseConnector.getConnection();
            DatabaseConnector.closeConnection(conn);
            assertTrue(conn.isClosed());
        });
    }

    @Test
    void testCloseNullConnection() {
        assertDoesNotThrow(() -> DatabaseConnector.closeConnection(null));
    }
}