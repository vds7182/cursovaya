package org.example;

import org.junit.jupiter.api.Test;
import java.sql.*;
import javax.swing.table.TableModel;
import static org.junit.jupiter.api.Assertions.*;

class DbUtilsTest {

    @Test
    void testResultSetToTableModel() throws SQLException {
        // Створюємо тестовий ResultSet
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "");
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT 1 as id, 'Test' as name LIMIT 1");

        TableModel model = DbUtils.resultSetToTableModel(rs);

        assertNotNull(model);
        assertEquals(1, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("id", model.getColumnName(0));
        assertEquals("name", model.getColumnName(1));
        assertEquals(1, model.getValueAt(0, 0));
        assertEquals("Test", model.getValueAt(0, 1));

        rs.close();
        statement.close();
        connection.close();
    }
}