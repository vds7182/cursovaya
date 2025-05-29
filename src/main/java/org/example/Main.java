package org.example;

import org.example.ProductManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {
    public Main() {
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

        // Обробник кнопки управління продуктами
        productsBtn.addActionListener(e -> {
            this.setVisible(false);
            ProductManagement.showProductManagement(this);
        });
        warehouseBtn.addActionListener(e -> {
            this.setVisible(false);
            WarehouseManagement.showWarehouseManagement(this);
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

    public void returnToMainMenu() {
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Встановлюємо системний вигляд
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Main mainMenu = new Main();
            mainMenu.setVisible(true);
        });
    }
}