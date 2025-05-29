package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.io.*;
import javax.imageio.ImageIO;

public class ProductManagement extends JFrame {
    private JTextField searchField;
    private JButton addButton, searchButton, backButton;
    private JPanel productsPanel;
    private JScrollPane scrollPane;

    public ProductManagement(JFrame mainMenu) {
        setTitle("Управління продуктами");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Верхня панель з кнопками
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        topPanel.setBackground(new Color(22, 31, 53));

        backButton = createStyledButton("← Повернутися", new Color(70, 130, 180));
        addButton = createStyledButton("＋ Додати продукт", new Color(34, 139, 34));
        searchButton = createStyledButton("🔍 Пошук", new Color(100, 149, 237));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        topPanel.add(backButton);
        topPanel.add(addButton);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Панель продуктів
        productsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        productsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        productsPanel.setBackground(new Color(240, 240, 240));

        scrollPane = new JScrollPane(productsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Обробники подій
        backButton.addActionListener(e -> {
            this.dispose();
            mainMenu.setVisible(true);
        });

        addButton.addActionListener(e -> showAddProductDialog());
        searchButton.addActionListener(e -> loadProducts(searchField.getText()));

        loadProducts("");
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadProducts(String searchTerm) {
        productsPanel.removeAll();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "SELECT id, name, description, price, image FROM products";
            if (!searchTerm.isEmpty()) {
                sql += " WHERE name LIKE ?";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (!searchTerm.isEmpty()) {
                pstmt.setString(1, "%" + searchTerm + "%");
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                byte[] imageData = rs.getBytes("image");

                productsPanel.add(createProductCard(id, name, description, price, imageData));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Помилка завантаження продуктів", "Помилка", JOptionPane.ERROR_MESSAGE);
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel createProductCard(int id, String name, String description, double price, byte[] imageData) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 400));

        // Область зображення
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(280, 200));
        imageLabel.setBackground(new Color(245, 245, 245));
        imageLabel.setOpaque(true);

        try {
            ImageIcon icon;
            if (imageData != null) {
                icon = new ImageIcon(imageData);
            } else {
                // Заглушка для відсутнього зображення
                BufferedImage placeholder = new BufferedImage(280, 200, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = placeholder.createGraphics();
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, 280, 200);
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString("Немає зображення", 80, 110);
                g2d.dispose();
                icon = new ImageIcon(placeholder);
            }
            imageLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                    280, 200, Image.SCALE_SMOOTH)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Інформація про продукт
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(null);

        JLabel priceLabel = new JLabel(String.format("Ціна: %.2f грн", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(descArea);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(priceLabel);

        // Кнопки управління
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton editButton = createActionButton("Редагувати", new Color(70, 130, 180));
        JButton deleteButton = createActionButton("Видалити", new Color(220, 53, 69));

        editButton.addActionListener(e -> showEditDialog(id));
        deleteButton.addActionListener(e -> deleteProduct(id));

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        card.add(imageLabel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(8, 0, 8, 0));
        return button;
    }

    private void showAddProductDialog() {
        ProductDialog dialog = new ProductDialog(this, "Додати новий продукт", -1);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadProducts(searchField.getText());
        }
    }

    private void showEditDialog(int productId) {
        ProductDialog dialog = new ProductDialog(this, "Редагувати продукт", productId);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadProducts(searchField.getText());
        }
    }

    private void deleteProduct(int productId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити цей продукт?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                String sql = "DELETE FROM products WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, productId);

                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Продукт видалено успішно!");
                    loadProducts(searchField.getText());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Помилка видалення продукту", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showProductManagement(JFrame mainMenu) {
        SwingUtilities.invokeLater(() -> {
            ProductManagement pm = new ProductManagement(mainMenu);
            pm.setVisible(true);
        });
    }

    // Внутрішній клас для діалогу додавання/редагування
    private class ProductDialog extends JDialog {
        private boolean success = false;
        private int productId;

        public ProductDialog(JFrame parent, String title, int productId) {
            super(parent, title, true);
            this.productId = productId;
            setSize(500, 500);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            setResizable(false);

            // Основні компоненти
            JPanel formPanel = new JPanel();
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            formPanel.setLayout(new GridLayout(5, 1, 10, 10));

            JTextField nameField = new JTextField();
            JTextArea descArea = new JTextArea();
            JScrollPane descScroll = new JScrollPane(descArea);
            JTextField priceField = new JTextField();
            JLabel imageLabel = new JLabel("", JLabel.CENTER);
            imageLabel.setPreferredSize(new Dimension(300, 200));
            imageLabel.setBorder(new LineBorder(Color.LIGHT_GRAY));

            JButton selectImageBtn = new JButton("Обрати зображення");
            File[] selectedFile = { null };

            // Заповнення даними для редагування
            if (productId > 0) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
                    String sql = "SELECT * FROM products WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, productId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        nameField.setText(rs.getString("name"));
                        descArea.setText(rs.getString("description"));
                        priceField.setText(String.valueOf(rs.getDouble("price")));

                        byte[] imageData = rs.getBytes("image");
                        if (imageData != null) {
                            ImageIcon icon = new ImageIcon(imageData);
                            imageLabel.setIcon(new ImageIcon(icon.getImage()
                                    .getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Обробник вибору зображення
            selectImageBtn.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedFile[0] = fileChooser.getSelectedFile();
                    ImageIcon icon = new ImageIcon(selectedFile[0].getAbsolutePath());
                    imageLabel.setIcon(new ImageIcon(icon.getImage()
                            .getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
                }
            });

            // Додавання компонентів
            formPanel.add(createFormField("Назва:", nameField));
            formPanel.add(createFormField("Опис:", descScroll));
            formPanel.add(createFormField("Ціна:", priceField));
            formPanel.add(selectImageBtn);
            formPanel.add(imageLabel);

            // Кнопки
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelBtn = new JButton("Скасувати");
            JButton saveBtn = new JButton("Зберегти");

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> {
                try {
                    String name = nameField.getText();
                    String description = descArea.getText();
                    double price = Double.parseDouble(priceField.getText());

                    if (name.isEmpty()) {
                        throw new Exception("Введіть назву продукту");
                    }

                    if (productId > 0) {
                        updateProduct(productId, name, description, price, selectedFile[0]);
                    } else {
                        saveProduct(name, description, price, selectedFile[0]);
                    }
                    success = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ProductDialog.this,
                            "Введіть коректну ціну", "Помилка", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProductDialog.this,
                            ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            });

            buttonPanel.add(cancelBtn);
            buttonPanel.add(saveBtn);

            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JPanel createFormField(String label, Component field) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel(label), BorderLayout.NORTH);
            panel.add(field, BorderLayout.CENTER);
            return panel;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    private void saveProduct(String name, String description, double price, File imageFile) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "INSERT INTO products (name, description, price, image) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);

            if (imageFile != null && imageFile.exists()) {
                try (InputStream is = new FileInputStream(imageFile);
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] data = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    buffer.flush();
                    pstmt.setBytes(4, buffer.toByteArray());
                }
            } else {
                pstmt.setNull(4, Types.BLOB);
            }

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Продукт успішно додано!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateProduct(int id, String name, String description, double price, File imageFile) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cyrsova", "root", "")) {
            String sql = "UPDATE products SET name = ?, description = ?, price = ?, image = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);

            if (imageFile != null && imageFile.exists()) {
                try (InputStream is = new FileInputStream(imageFile);
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] data = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    buffer.flush();
                    pstmt.setBytes(4, buffer.toByteArray());
                }
            } else {
                // Якщо зображення не вибрано, зберігаємо старе
                try (PreparedStatement ps = conn.prepareStatement("SELECT image FROM products WHERE id = ?")) {
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        pstmt.setBytes(4, rs.getBytes("image"));
                    } else {
                        pstmt.setNull(4, Types.BLOB);
                    }
                }
            }

            pstmt.setInt(5, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Продукт оновлено успішно!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}