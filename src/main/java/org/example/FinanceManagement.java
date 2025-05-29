package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.*;

public class FinanceManagement extends JFrame {
    private JTable transactionsTable;
    private JButton addButton, backButton, filterButton;
    private JComboBox<String> periodCombo;
    private JLabel balanceLabel;
    private JPanel chartPanel;

    public FinanceManagement(JFrame mainMenu) {
        setTitle("Управління фінансами");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Верхня панель
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(22, 31, 53));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        backButton = createStyledButton("← Повернутися", new Color(70, 130, 180));
        addButton = createStyledButton("＋ Додати транзакцію", new Color(34, 139, 34));
        buttonPanel.add(backButton);
        buttonPanel.add(addButton);

        // Панель фільтрів
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        String[] periods = {"Сьогодні", "Цей тиждень", "Цей місяць", "Цей рік", "Усі"};
        periodCombo = new JComboBox<>(periods);
        filterButton = createStyledButton("Фільтрувати", new Color(100, 149, 237));
        filterPanel.add(new JLabel("Період:"));
        filterPanel.add(periodCombo);
        filterPanel.add(filterButton);

        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Основна панель
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Таблиця транзакцій
        transactionsTable = new JTable();
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(transactionsTable);

        // Панель статистики
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Баланс
        balanceLabel = new JLabel("Поточний баланс: 0.00 грн", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statsPanel.add(balanceLabel, BorderLayout.NORTH);

        // Графік (заглушка)
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Тут буде логіка малювання графіка
                g.setColor(Color.BLUE);
                g.drawString("Графік доходів/витрат буде тут", 50, 50);
            }
        };
        chartPanel.setPreferredSize(new Dimension(300, 300));
        statsPanel.add(chartPanel, BorderLayout.CENTER);

        mainPanel.add(tableScroll);
        mainPanel.add(statsPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Обробники подій
        backButton.addActionListener(e -> {
            this.dispose();
            mainMenu.setVisible(true);
        });

        addButton.addActionListener(e -> showAddTransactionDialog());
        filterButton.addActionListener(e -> loadTransactions());

        // Завантажити дані
        loadTransactions();
        updateBalance();
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

    private void loadTransactions() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "SELECT t.id, t.date, t.amount, t.type, t.description, p.name as product_name " +
                    "FROM transactions t " +
                    "LEFT JOIN products p ON t.product_id = p.id " +
                    "WHERE 1=1";

            // Додаємо фільтрацію за періодом
            String period = (String) periodCombo.getSelectedItem();
            switch (period) {
                case "Сьогодні":
                    sql += " AND DATE(t.date) = CURDATE()";
                    break;
                case "Цей тиждень":
                    sql += " AND YEARWEEK(t.date, 1) = YEARWEEK(CURDATE(), 1)";
                    break;
                case "Цей місяць":
                    sql += " AND MONTH(t.date) = MONTH(CURDATE()) AND YEAR(t.date) = YEAR(CURDATE())";
                    break;
                case "Цей рік":
                    sql += " AND YEAR(t.date) = YEAR(CURDATE())";
                    break;
            }

            sql += " ORDER BY t.date DESC";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // Модель таблиці
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Дата", "Сума", "Тип", "Опис", "Товар"}, 0) {
                @Override
                public Class<?> getColumnClass(int column) {
                    switch (column) {
                        case 0: return Integer.class;
                        case 2: return Double.class;
                        default: return String.class;
                    }
                }
            };

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("uk", "UA"));

            while (rs.next()) {
                String type = rs.getString("type");
                Color rowColor = "Дохід".equals(type) ? new Color(220, 255, 220) : new Color(255, 220, 220);

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp("date"),
                        rs.getDouble("amount"),
                        type,
                        rs.getString("description"),
                        rs.getString("product_name")
                });
            }

            transactionsTable.setModel(model);

            // Додаємо рендерер для кольорів рядків
            transactionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    String type = (String) table.getModel().getValueAt(row, 3);
                    if ("Дохід".equals(type)) {
                        c.setBackground(new Color(220, 255, 220));
                    } else {
                        c.setBackground(new Color(255, 220, 220));
                    }

                    if (isSelected) {
                        c.setBackground(new Color(150, 180, 255));
                    }

                    // Форматування для колонки "Сума"
                    if (column == 2) {
                        setHorizontalAlignment(JLabel.RIGHT);
                        setText(currencyFormat.format(value));
                    } else {
                        setHorizontalAlignment(JLabel.LEFT);
                    }

                    return c;
                }
            });

            // Налаштування ширини стовпців
            transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
            transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка завантаження транзакцій", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBalance() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "SELECT " +
                    "SUM(CASE WHEN type = 'Дохід' THEN amount ELSE 0 END) as income, " +
                    "SUM(CASE WHEN type = 'Витрата' THEN amount ELSE 0 END) as expense " +
                    "FROM transactions";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double income = rs.getDouble("income");
                double expense = rs.getDouble("expense");
                double balance = income - expense;

                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("uk", "UA"));
                balanceLabel.setText(String.format(
                        "<html><center>Поточний баланс: <b>%s</b><br>" +
                                "Доходи: <font color='green'>%s</font><br>" +
                                "Витрати: <font color='red'>%s</font></center></html>",
                        format.format(balance),
                        format.format(income),
                        format.format(expense)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAddTransactionDialog() {
        JDialog dialog = new JDialog(this, "Додати транзакцію", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Поля форми
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Дохід", "Витрата"});
        JTextField amountField = new JTextField(15);
        JComboBox<String> productCombo = new JComboBox<>();
        loadProductsToCombo(productCombo);
        JTextArea descriptionArea = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JButton saveBtn = new JButton("Зберегти");
        JButton cancelBtn = new JButton("Скасувати");

        // Розміщення компонентів
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Тип:"), gbc);
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        dialog.add(new JLabel("Сума:"), gbc);
        gbc.gridx = 1;
        dialog.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        dialog.add(new JLabel("Товар (необов'язково):"), gbc);
        gbc.gridx = 1;
        dialog.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        dialog.add(new JLabel("Опис:"), gbc);
        gbc.gridx = 1;
        dialog.add(descriptionScroll, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        dialog.add(buttonPanel, gbc);

        // Обробник збереження
        saveBtn.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String product = (String) productCombo.getSelectedItem();
                String description = descriptionArea.getText();

                if (amount <= 0) {
                    throw new Exception("Сума має бути більше нуля");
                }

                addTransaction(type, amount, product, description);
                loadTransactions();
                updateBalance();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введіть коректну суму", "Помилка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void loadProductsToCombo(JComboBox<String> combo) {
        combo.addItem("Не пов'язано з товаром");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "SELECT id, name FROM products";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTransaction(String type, double amount, String productName, String description) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "INSERT INTO transactions (date, amount, type, description, product_id) " +
                    "VALUES (NOW(), ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, type);
            pstmt.setString(3, description);

            if (productName != null && !productName.equals("Не пов'язано з товаром")) {
                int productId = getProductId(conn, productName);
                pstmt.setInt(4, productId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Транзакцію додано успішно!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private int getProductId(Connection conn, String productName) throws SQLException {
        String sql = "SELECT id FROM products WHERE name = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, productName);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }
        throw new SQLException("Товар не знайдений");
    }

    public static void showFinanceManagement(JFrame mainMenu) {
        SwingUtilities.invokeLater(() -> {
            FinanceManagement fm = new FinanceManagement(mainMenu);
            fm.setVisible(true);
        });
    }
}