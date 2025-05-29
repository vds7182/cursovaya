package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseManagement extends JFrame {
    private JTable inventoryTable;
    private JButton addButton, editButton, deleteButton, backButton;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;

    public WarehouseManagement(JFrame mainMenu) {
        setTitle("Управління складом");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Верхня панель з кнопками
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(22, 31, 53));

        backButton = createStyledButton("← Повернутися", new Color(70, 130, 180));
        addButton = createStyledButton("＋ Додати", new Color(34, 139, 34));
        editButton = createStyledButton("✎ Редагувати", new Color(255, 165, 0));
        deleteButton = createStyledButton("✖ Видалити", new Color(220, 53, 69));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] filters = {"Всі", "На складі", "Закінчується", "Відсутній"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        topPanel.add(backButton);
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(new JLabel("Пошук:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Фільтр:"));
        topPanel.add(filterComboBox);

        add(topPanel, BorderLayout.NORTH);

        // Таблиця інвентарю
        inventoryTable = new JTable();
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Обробники подій
        backButton.addActionListener(e -> {
            this.dispose();
            mainMenu.setVisible(true);
        });

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteItem());

        searchField.addActionListener(e -> loadInventory());
        filterComboBox.addActionListener(e -> loadInventory());

        // Завантажити дані
        loadInventory();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadInventory() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "SELECT i.id, p.name, i.quantity, i.location, i.last_updated " +
                    "FROM inventory i " +
                    "JOIN products p ON i.product_id = p.id " +
                    "WHERE p.name LIKE ?";

            // Додаємо фільтрацію
            String filter = (String) filterComboBox.getSelectedItem();
            switch (filter) {
                case "На складі":
                    sql += " AND i.quantity > 10";
                    break;
                case "Закінчується":
                    sql += " AND i.quantity > 0 AND i.quantity <= 10";
                    break;
                case "Відсутній":
                    sql += " AND i.quantity = 0";
                    break;
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchField.getText() + "%");

            ResultSet rs = pstmt.executeQuery();

            // Створюємо модель таблиці
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Продукт", "Кількість", "Розташування", "Останнє оновлення"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 2 ? Integer.class : String.class;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("location"),
                        rs.getTimestamp("last_updated")
                });
            }

            inventoryTable.setModel(model);
            inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка завантаження інвентарю", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        InventoryDialog dialog = new InventoryDialog(this, "Додати новий запис", -1);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadInventory();
        }
    }

    private void showEditDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Виберіть запис для редагування", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) inventoryTable.getValueAt(selectedRow, 0);
        InventoryDialog dialog = new InventoryDialog(this, "Редагувати запис", id);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadInventory();
        }
    }

    private void deleteItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Виберіть запис для видалення", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити цей запис?",
                "Підтвердження видалення", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) inventoryTable.getValueAt(selectedRow, 0);
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "DELETE FROM inventory WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);

                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Запис видалено успішно!");
                    loadInventory();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Помилка видалення запису", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showWarehouseManagement(JFrame mainMenu) {
        SwingUtilities.invokeLater(() -> {
            WarehouseManagement wm = new WarehouseManagement(mainMenu);
            wm.setVisible(true);
        });
    }

    // Внутрішній клас для діалогу додавання/редагування
    private class InventoryDialog extends JDialog {
        private boolean success = false;
        private int inventoryId;
        private JComboBox<String> productCombo;
        private JTextField quantityField;
        private JTextField locationField;

        public InventoryDialog(JFrame parent, String title, int inventoryId) {
            super(parent, title, true);
            this.inventoryId = inventoryId;
            setSize(400, 300);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            setResizable(false);

            // Основні компоненти
            JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Список продуктів
            List<String> products = getProductsList();
            productCombo = new JComboBox<>(products.toArray(new String[0]));
            productCombo.setFont(new Font("Arial", Font.PLAIN, 14));

            quantityField = new JTextField();
            locationField = new JTextField();

            formPanel.add(new JLabel("Продукт:"));
            formPanel.add(productCombo);
            formPanel.add(new JLabel("Кількість:"));
            formPanel.add(quantityField);
            formPanel.add(new JLabel("Розташування:"));
            formPanel.add(locationField);

            // Заповнення даними для редагування
            if (inventoryId > 0) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                    String sql = "SELECT i.product_id, p.name, i.quantity, i.location " +
                            "FROM inventory i " +
                            "JOIN products p ON i.product_id = p.id " +
                            "WHERE i.id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, inventoryId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        productCombo.setSelectedItem(rs.getString("name"));
                        quantityField.setText(String.valueOf(rs.getInt("quantity")));
                        locationField.setText(rs.getString("location"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Кнопки
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelBtn = new JButton("Скасувати");
            JButton saveBtn = new JButton("Зберегти");

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> {
                try {
                    String productName = (String) productCombo.getSelectedItem();
                    int quantity = Integer.parseInt(quantityField.getText());
                    String location = locationField.getText();

                    if (productName == null || productName.isEmpty()) {
                        throw new Exception("Виберіть продукт");
                    }
                    if (quantity < 0) {
                        throw new Exception("Кількість не може бути від'ємною");
                    }
                    if (location.isEmpty()) {
                        throw new Exception("Введіть розташування");
                    }

                    if (inventoryId > 0) {
                        updateInventory(inventoryId, productName, quantity, location);
                    } else {
                        addInventory(productName, quantity, location);
                    }
                    success = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(InventoryDialog.this,
                            "Введіть коректну кількість", "Помилка", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InventoryDialog.this,
                            ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            });

            buttonPanel.add(cancelBtn);
            buttonPanel.add(saveBtn);

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private List<String> getProductsList() {
            List<String> products = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "SELECT name FROM products";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    products.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return products;
        }

        private int getProductId(String productName) throws SQLException {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "SELECT id FROM products WHERE name = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, productName);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
            throw new SQLException("Продукт не знайдений");
        }

        private void addInventory(String productName, int quantity, String location) throws SQLException {
            int productId = getProductId(productName);

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "INSERT INTO inventory (product_id, quantity, location) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, location);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Запис додано успішно!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void updateInventory(int inventoryId, String productName, int quantity, String location) throws SQLException {
            int productId = getProductId(productName);

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "UPDATE inventory SET product_id = ?, quantity = ?, location = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, location);
                pstmt.setInt(4, inventoryId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Запис оновлено успішно!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        public boolean isSuccess() {
            return success;
        }
    }
}