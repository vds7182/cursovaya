package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;

public class Main extends JFrame {
    private Connection dbConnection;

    public Main(Connection connection) {
        this.dbConnection = connection;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Головне меню");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Стилізований інтерфейс
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        mainPanel.setBackground(new Color(22, 31, 53));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 15, 0);

        // Заголовок
        JLabel titleLabel = new JLabel("Система управління супермаркетом", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));

        // Кнопки меню
        JButton productsBtn = createMenuButton("Управління продуктами");
        JButton warehouseBtn = createMenuButton("Управління складом");
        JButton financeBtn = createMenuButton("Управління фінансами");
        JButton reportsBtn = createMenuButton("Звіти та аналітика");

        // Додаємо компоненти
        mainPanel.add(titleLabel, gbc);
        mainPanel.add(productsBtn, gbc);
        mainPanel.add(warehouseBtn, gbc);
        mainPanel.add(financeBtn, gbc);
        mainPanel.add(reportsBtn, gbc);

        // Обробники подій
        productsBtn.addActionListener(e -> {
            this.setVisible(false);
            ProductManagement.showProductManagement(this);
        });

        warehouseBtn.addActionListener(e -> {
            this.setVisible(false);
            WarehouseManagement.showWarehouseManagement(this);
        });

        financeBtn.addActionListener(e -> {
            this.setVisible(false);
            FinanceManagement.showFinanceManagement(this);
        });

        reportsBtn.addActionListener(e -> {
            this.setVisible(false);
            zvitmenu.showReportsManagement(this, dbConnection);
        });

        add(mainPanel);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Встановлюємо системний вигляд
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Отримуємо підключення до БД
                Connection connection = DatabaseConnector.getConnection();

                // Створюємо головне меню
                Main mainMenu = new Main(connection);
                mainMenu.setVisible(true);

                // Додаємо обробник закриття вікна
                mainMenu.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        DatabaseConnector.closeConnection(connection);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Помилка ініціалізації: " + e.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}