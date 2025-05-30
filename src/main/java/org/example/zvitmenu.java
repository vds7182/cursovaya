package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class zvitmenu extends JFrame {
    private JButton backButton;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private Connection connection;

    public zvitmenu(JFrame mainMenu, Connection connection) {
        this.connection = connection;
        setTitle("Управління звітами");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Головна панель з кнопками вибору звітів
        JPanel menuPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        menuPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton financeButton = createStyledButton("Звіти по фінансах", new Color(34, 139, 34));
        JButton inventoryButton = createStyledButton("Звіти по складу", new Color(70, 130, 180));
        JButton productsButton = createStyledButton("Звіти по товарах", new Color(128, 0, 128));

        menuPanel.add(financeButton);
        menuPanel.add(inventoryButton);
        menuPanel.add(productsButton);

        // Панель з картками для різних звітів
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Додаємо панелі звітів
        cardsPanel.add(createFinanceReportPanel(), "finance");
        cardsPanel.add(createInventoryReportPanel(), "inventory");
        cardsPanel.add(createProductsReportPanel(), "products");

        // Кнопка "Назад"
        backButton = createStyledButton("← Повернутися", new Color(169, 169, 169));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(backButton);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Додаємо компоненти до фрейму
        add(menuPanel, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Обробники подій
        backButton.addActionListener(e -> {
            this.dispose();
            mainMenu.setVisible(true);
        });

        financeButton.addActionListener(e -> cardLayout.show(cardsPanel, "finance"));
        inventoryButton.addActionListener(e -> cardLayout.show(cardsPanel, "inventory"));
        productsButton.addActionListener(e -> cardLayout.show(cardsPanel, "products"));
    }

    private JPanel createFinanceReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Фінансові звіти"));

        // Панель для вибору дати
        JPanel datePanel = new JPanel();
        JLabel dateLabel = new JLabel("Оберіть дату (yyyy-MM-dd):");
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 15);
        JButton showButton = new JButton("Показати");

        datePanel.add(dateLabel);
        datePanel.add(dateField);
        datePanel.add(showButton);

        // Таблиця для результатів
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        // Панель для підсумків
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2));
        JLabel incomeLabel = new JLabel("Загальний дохід: 0.00", SwingConstants.CENTER);
        JLabel expenseLabel = new JLabel("Загальні витрати: 0.00", SwingConstants.CENTER);
        incomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        expenseLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);

        panel.add(datePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        showButton.addActionListener(e -> {
            try {
                String date = dateField.getText();

                // Перевірка коректності дати
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                sdf.parse(date); // Якщо дата некоректна, буде виняток

                // Запит для транзакцій
                String query = "SELECT t.id, t.date, t.amount, t.type, t.description, p.name " +
                        "FROM transactions t LEFT JOIN products p ON t.product_id = p.id " +
                        "WHERE DATE(t.date) = ? ORDER BY t.date";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, date);
                    ResultSet rs = stmt.executeQuery();
                    table.setModel(DbUtils.resultSetToTableModel(rs));
                }

                // Запит для підрахунку доходів/витрат
                String sumQuery = "SELECT " +
                        "SUM(CASE WHEN type = 'Дохід' THEN amount ELSE 0 END) as total_income, " +
                        "SUM(CASE WHEN type = 'Витрата' THEN amount ELSE 0 END) as total_expense " +
                        "FROM transactions WHERE DATE(date) = ?";

                try (PreparedStatement sumStmt = connection.prepareStatement(sumQuery)) {
                    sumStmt.setString(1, date);
                    ResultSet sumRs = sumStmt.executeQuery();

                    if (sumRs.next()) {
                        double income = sumRs.getDouble("total_income");
                        double expense = sumRs.getDouble("total_expense");

                        incomeLabel.setText("Загальний дохід: " + String.format("%.2f", income));
                        expenseLabel.setText("Загальні витрати: " + String.format("%.2f", expense));
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Помилка: " + ex.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        return panel;
    }

    private JPanel createInventoryReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Звіти по складу"));

        // Панель для вибору дати
        JPanel datePanel = new JPanel();
        JLabel dateLabel = new JLabel("Оберіть дату:");
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        JButton showButton = new JButton("Показати");

        datePanel.add(dateLabel);
        datePanel.add(dateField);
        datePanel.add(showButton);

        // Таблиця для результатів
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(datePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        showButton.addActionListener(e -> {
            try {
                String date = dateField.getText();
                String query = "SELECT w.id, p.name, w.quantity, w.location, w.last_updated " +
                        "FROM inventory w JOIN products p ON w.product_id = p.id " +
                        "WHERE DATE(w.last_updated) = ? ORDER BY p.name";

                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                table.setModel(DbUtils.resultSetToTableModel(rs));

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createProductsReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Звіти по товарах"));

        // Панель для вибору дати
        JPanel datePanel = new JPanel();
        JLabel dateLabel = new JLabel("Оберіть дату:");
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        JButton showButton = new JButton("Показати");

        datePanel.add(dateLabel);
        datePanel.add(dateField);
        datePanel.add(showButton);

        // Таблиця для результатів
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        // Панель для підсумків
        JLabel totalLabel = new JLabel("Загальна вартість товарів: 0.00");
        JPanel summaryPanel = new JPanel();
        summaryPanel.add(totalLabel);

        panel.add(datePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        showButton.addActionListener(e -> {
            try {
                String date = dateField.getText();
                String query = "SELECT p.id, p.name, p.description, p.price, p.add_date " +
                        "FROM products p " +
                        "WHERE DATE(p.add_date) = ? ORDER BY p.name";

                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                table.setModel(DbUtils.resultSetToTableModel(rs));

                // Підрахунок загальної вартості
                String sumQuery = "SELECT SUM(price) as total FROM products WHERE DATE(add_date) = ?";
                PreparedStatement sumStmt = connection.prepareStatement(sumQuery);
                sumStmt.setString(1, date);
                ResultSet sumRs = sumStmt.executeQuery();

                if (sumRs.next()) {
                    double total = sumRs.getDouble("total");
                    totalLabel.setText("Загальна вартість товарів: " + String.format("%.2f", total));
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
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

    public static void showReportsManagement(JFrame mainMenu, Connection connection) {
        SwingUtilities.invokeLater(() -> {
            zvitmenu reports = new zvitmenu(mainMenu, connection);
            reports.setLocationRelativeTo(mainMenu);
            reports.setVisible(true);
        });
    }
}

// Допоміжний клас для перетворення ResultSet в TableModel
class DbUtils {
    // Alternative implementation using arrays
    public static javax.swing.table.TableModel resultSetToTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Get column names
        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        // Get data
        Vector<Object[]> data = new Vector<>();
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i-1] = rs.getObject(i);
            }
            data.add(row);
        }

        // Convert Object[] to Vector<Object>
        Vector<Vector<Object>> vectorData = new Vector<>();
        for (Object[] row : data) {
            Vector<Object> vectorRow = new Vector<>();
            for (Object cell : row) {
                vectorRow.add(cell);
            }
            vectorData.add(vectorRow);
        }

        return new DefaultTableModel(vectorData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}