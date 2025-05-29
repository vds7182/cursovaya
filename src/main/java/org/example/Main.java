package org.example;

import java.io.FileNotFoundException;
import java.sql.*;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main implements ActionListener {
    private Frame mainFrame;
    private JFrame productsFrame;
    private JFrame warehouseFrame;
    private JFrame financeFrame;
    private JFrame reportsFrame;

    Main() {

        // Create main frame
        mainFrame = new Frame("Управління супермаркетом");
        mainFrame.setSize(800, 600);
        mainFrame.setLayout(null);

        // Add image label (replace with your actual image path)
        JLabel imageLabel = new JLabel(new ImageIcon("supermarket.jpg")); // Change to your image path
        imageLabel.setBounds(0, 0, 800, 400);
        mainFrame.add(imageLabel);

        // Create buttons
        Button b = new Button("Управління продуктами");
        b.setBounds(20, 450, 200, 100);
        b.addActionListener(this);
        mainFrame.add(b);

        Button b1 = new Button("Управління складом");
        b1.setBounds(220, 450, 200, 100);
        b1.addActionListener(this);
        mainFrame.add(b1);

        Button b2 = new Button("Управління фінансами");
        b2.setBounds(420, 450, 200, 100);
        b2.addActionListener(this);
        mainFrame.add(b2);

        Button b3 = new Button("Звіти та аналітика");
        b3.setBounds(620, 450, 175, 100);
        b3.addActionListener(this);
        mainFrame.add(b3);

        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // Hide main frame
        mainFrame.setVisible(false);

        switch (command) {
            case "Управління продуктами":
                showProductsWindow();
                break;
            case "Управління складом":
                showWarehouseWindow();
                break;
            case "Управління фінансами":
                showFinanceWindow();
                break;
            case "Звіти та аналітика":
                showReportsWindow();
                break;
        }
    }

    private void showProductsWindow() {
        if (productsFrame == null) {
            productsFrame = new JFrame("Управління продуктами");
            productsFrame.setSize(800, 600);
            productsFrame.setLayout(new BorderLayout());

            // Add content
            JLabel label = new JLabel("Тут буде інтерфейс управління продуктами", SwingConstants.CENTER);
            productsFrame.add(label, BorderLayout.CENTER);

            // Add return button
            productsFrame.setLayout(null); // Вимкнути менеджер розташування

            JButton returnButton = new JButton("Повернутися");
            returnButton.setBounds(10, 10, 100, 50); // x, y, width, height
            returnButton.addActionListener(e -> {
                productsFrame.setVisible(false);
                mainFrame.setVisible(true);
            });
            productsFrame.add(returnButton);
            JButton add_product= new JButton("Додати новий продукт");
            add_product.setBounds(500, 10, 300, 50);
            add_product.addActionListener(e -> {
                JFrame add_product_frame = new JFrame("Новий продукт");
                add_product_frame.setSize(500, 400);

                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(6, 2, 10, 10));

                // Додаємо компоненти
                panel.add(new JLabel("Назва продукту:"));
                JTextField nameField = new JTextField();
                panel.add(nameField);

                panel.add(new JLabel("Опис:"));
                JTextField descriptionField = new JTextField();
                panel.add(descriptionField);

                panel.add(new JLabel("Ціна:"));
                JTextField priceField = new JTextField();
                panel.add(priceField);

                panel.add(new JLabel("Зображення:"));
                JButton selectImageBtn = new JButton("Обрати зображення");
                panel.add(selectImageBtn);

                JButton saveButton = new JButton("Зберегти");
                panel.add(saveButton);

                JButton backButton = new JButton("Назад");
                backButton.addActionListener(ev -> add_product_frame.dispose());
                panel.add(backButton);

                // Змінна для зберігання обраного файлу

                // Обробник кнопки вибору зображення
                selectImageBtn.addActionListener(ev -> {
                    File imageFile;
                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        imageFile = fileChooser.getSelectedFile();
                        selectImageBtn.setText(imageFile.getName());
                    }
                });

                saveButton.addActionListener(a -> {
                    // Отримуємо дані з полів введення
                    final String name = nameField.getText();  // Явно робимо final
                    final String description = descriptionField.getText();
                    final String priceText = priceField.getText();

                    // Валідація даних
                    if (name.isEmpty() || priceText.isEmpty()) {
                        JOptionPane.showMessageDialog(add_product_frame,
                                "Будь ласка, заповніть назву та ціну",
                                "Помилка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        final double price = Double.parseDouble(priceText);  // Явно робимо final

                        // Підключення до бази даних
                        final String url = "jdbc:mysql://localhost:3306/cyrsova";  // final
                        final String user = "root";  // final
                        final String password = "Marvel229";  // final

                        // Створюємо копію посилання на imageFile
                        final File imageFileCopy = null;

                        new Thread(() -> {  // Виконуємо в окремому потоці, щоб не блокувати UI
                            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                                String sql = "INSERT INTO products (name, description, price, image) VALUES (?, ?, ?, ?)";
                                PreparedStatement pstmt = conn.prepareStatement(sql);

                                pstmt.setString(1, name);
                                pstmt.setString(2, description);
                                pstmt.setDouble(3, price);

                                if (imageFileCopy != null) {
                                    FileInputStream fis = new FileInputStream(imageFileCopy);
                                    pstmt.setBinaryStream(4, fis, (int)imageFileCopy.length());
                                } else {
                                    pstmt.setNull(4, Types.BLOB);
                                }

                                int rowsAffected = pstmt.executeUpdate();

                                SwingUtilities.invokeLater(() -> {
                                    if (rowsAffected > 0) {
                                        JOptionPane.showMessageDialog(add_product_frame,
                                                "Продукт успішно додано!",
                                                "Успіх", JOptionPane.INFORMATION_MESSAGE);
                                        add_product_frame.dispose();
                                    }
                                });
                            } catch (Exception p) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(add_product_frame,
                                            "Помилка при збереженні: " + p.getMessage(),
                                            "Помилка", JOptionPane.ERROR_MESSAGE);
                                });
                                p.printStackTrace();
                            }
                        }).start();

                    } catch (NumberFormatException k) {
                        JOptionPane.showMessageDialog(add_product_frame,
                                "Будь ласка, введіть коректну ціну",
                                "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                add_product_frame.add(panel);
                add_product_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                add_product_frame.setVisible(true);
            });
            productsFrame.add(add_product);
            productsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            productsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mainFrame.setVisible(true);
                }
            });
        }
        productsFrame.setVisible(true);
    }

    private void showWarehouseWindow() {
        if (warehouseFrame == null) {
            warehouseFrame = new JFrame("Управління складом");
            warehouseFrame.setSize(800, 600);
            warehouseFrame.setLayout(new BorderLayout());

            // Add content
            JLabel label = new JLabel("Тут буде інтерфейс управління складом", SwingConstants.CENTER);
            warehouseFrame.add(label, BorderLayout.CENTER);

            // Add return button
            warehouseFrame.setLayout(null);
            JButton returnButton = new JButton("Повернутися");
            returnButton.setBounds(10, 10, 100, 50); // x, y, width, height
            returnButton.addActionListener(e -> {
                warehouseFrame.setVisible(false);
                mainFrame.setVisible(true);
            });
            warehouseFrame.add(returnButton);

            warehouseFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            warehouseFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mainFrame.setVisible(true);
                }
            });
        }
        warehouseFrame.setVisible(true);
    }

    private void showFinanceWindow() {
        if (financeFrame == null) {
            financeFrame = new JFrame("Управління фінансами");
            financeFrame.setSize(800, 600);
            financeFrame.setLayout(new BorderLayout());

            // Add content
            JLabel label = new JLabel("Тут буде інтерфейс управління фінансами", SwingConstants.CENTER);
            financeFrame.add(label, BorderLayout.CENTER);

            // Add return button
            financeFrame.setLayout(null);
            JButton returnButton = new JButton("Повернутися");
            returnButton.setBounds(10, 10, 100, 50); // x, y, width, height
            returnButton.addActionListener(e -> {
                financeFrame.setVisible(false);
                mainFrame.setVisible(true);
            });
            financeFrame.add(returnButton);

            financeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            financeFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mainFrame.setVisible(true);
                }
            });
        }
        financeFrame.setVisible(true);
    }

    private void showReportsWindow() {
        if (reportsFrame == null) {
            reportsFrame = new JFrame("Звіти та аналітика");
            reportsFrame.setSize(800, 600);
            reportsFrame.setLayout(new BorderLayout());

            // Add content
            JLabel label = new JLabel("Тут буде інтерфейс звітів та аналітики", SwingConstants.CENTER);
            reportsFrame.add(label, BorderLayout.CENTER);
            reportsFrame.setLayout(null);

            // Add return button
            JButton returnButton = new JButton("Повернутися");
            returnButton.setBounds(10, 10, 100, 50); // x, y, width, height
            returnButton.addActionListener(e -> {
                reportsFrame.setVisible(false);
                mainFrame.setVisible(true);
            });
            reportsFrame.add(returnButton);

            reportsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            reportsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    mainFrame.setVisible(true);
                }
            });
        }
        reportsFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}