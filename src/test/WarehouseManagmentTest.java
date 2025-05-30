package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseManagementTest {

    private WarehouseManagement warehouseManagement;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "");
        JFrame mainMenu = new JFrame();
        warehouseManagement = new WarehouseManagement(mainMenu);
    }

    @Test
    void testCreateStyledButton() {
        JButton button = warehouseManagement.createStyledButton("Test", Color.RED);
        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertEquals(Color.RED, button.getBackground());
    }

    @Test
    void testLoadInventory() {
        assertDoesNotThrow(() -> warehouseManagement.loadInventory());
    }
}