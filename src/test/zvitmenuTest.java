package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ZvitmenuTest {

    private zvitmenu reportsManagement;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "");
        JFrame mainMenu = new JFrame();
        reportsManagement = new zvitmenu(mainMenu, testConnection);
    }

    @Test
    void testCreateStyledButton() {
        JButton button = reportsManagement.createStyledButton("Test", Color.ORANGE);
        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertEquals(Color.ORANGE, button.getBackground());
    }

    @Test
    void testCreateFinanceReportPanel() {
        assertNotNull(reportsManagement.createFinanceReportPanel());
    }

    @Test
    void testCreateInventoryReportPanel() {
        assertNotNull(reportsManagement.createInventoryReportPanel());
    }

    @Test
    void testCreateProductsReportPanel() {
        assertNotNull(reportsManagement.createProductsReportPanel());
    }
}