package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class FinanceManagementTest {

    private FinanceManagement financeManagement;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "");
        JFrame mainMenu = new JFrame();
        financeManagement = new FinanceManagement(mainMenu);
    }

    @Test
    void testCreateStyledButton() {
        JButton button = financeManagement.createStyledButton("Test", Color.GREEN);
        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertEquals(Color.GREEN, button.getBackground());
    }

    @Test
    void testLoadTransactions() {
        assertDoesNotThrow(() -> financeManagement.loadTransactions());
    }

    @Test
    void testUpdateBalance() {
        assertDoesNotThrow(() -> financeManagement.updateBalance());
    }
}