package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ProductManagementTest {

    private ProductManagement productManagement;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "");
        JFrame mainMenu = new JFrame();
        productManagement = new ProductManagement(mainMenu);
    }

    @Test
    void testCreateStyledButton() {
        JButton button = productManagement.createStyledButton("Test", Color.BLUE);
        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertEquals(Color.BLUE, button.getBackground());
    }

    @Test
    void testLoadProducts() {
        assertDoesNotThrow(() -> productManagement.loadProducts(""));
    }

    @Test
    void testDeleteProduct() {
        // Спочатку додамо тестовий продукт
        assertDoesNotThrow(() -> {
            try (var stmt = testConnection.prepareStatement(
                    "INSERT INTO products (name, description, price) VALUES (?, ?, ?)")) {
                stmt.setString(1, "Test Product");
                stmt.setString(2, "Test Description");
                stmt.setDouble(3, 10.99);
                stmt.executeUpdate();
            }

            // Отримуємо ID доданого продукту
            try (var stmt = testConnection.createStatement();
                 var rs = stmt.executeQuery("SELECT id FROM products WHERE name = 'Test Product'")) {
                if (rs.next()) {
                    int productId = rs.getInt("id");
                    productManagement.deleteProduct(productId);

                    // Перевіряємо, що продукт видалено
                    try (var checkStmt = testConnection.prepareStatement(
                            "SELECT COUNT(*) FROM products WHERE id = ?")) {
                        checkStmt.setInt(1, productId);
                        var rsCheck = checkStmt.executeQuery();
                        if (rsCheck.next()) {
                            assertEquals(0, rsCheck.getInt(1));
                        }
                    }
                }
            }
        });
    }
}