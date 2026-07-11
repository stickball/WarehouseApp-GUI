import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        com.formdev.flatlaf.FlatDarkLaf.setup();

        JTextField hostField = new JTextField("localhost:3306");
        JTextField dbNameField = new JTextField("warehouse_db");
        JTextField userField = new JTextField("root");
        JPasswordField passField = new JPasswordField("");

        JPanel loginpanel = new JPanel(new GridLayout(4,2,5,5));
        loginpanel.add(new JLabel("Server (IP:PORT):"));
        loginpanel.add(hostField);
        loginpanel.add(new JLabel("Όνομα Βάσης (DB):"));
        loginpanel.add(dbNameField);
        loginpanel.add(new JLabel("Username:"));
        loginpanel.add(userField);
        loginpanel.add(new JLabel("Password:"));
        loginpanel.add(passField);

        int loginResult = JOptionPane.showConfirmDialog(null, loginpanel,
                "Σύνδεση στη Βάση Δεδομένων", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (loginResult != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        String host = hostField.getText().trim();
        String dbName = dbNameField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        DBmanager db = new DBmanager(host, dbName, username, password);

        if (!db.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Αδυναμία σύνδεσης στη Βάση Δεδομένων!\n" +
                    "Ελέγξτε τα στοιχεία σας και βεβαιωθείτε ότι ο MySQL Server τρέχει.",
                    "Σφάλμα Σύνδεσης", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        JFrame frame = new JFrame("Διαχείριση Αποθήκης - GUI");

        try {
            java.net.URL iconURL = Main.class.getResource("/app_icon.png");
            if (iconURL != null) {
                ImageIcon img = new ImageIcon(iconURL);
                frame.setIconImage(img.getImage());
            }
        } catch (Exception ex) {
            System.out.println("Το εικονίδιο δεν βρέθηκε.");
        }

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());

        String[] columns = {"ID", "Όνομα", "Κατηγορία", "Ποσότητα", "Τιμή (€)"};

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 ) return Integer.class;
                if (columnIndex == 3 ) return Integer.class;
                if (columnIndex == 4 ) return Double.class;
                return String.class;
            }
        };

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        List<Product> products = db.getAllProducts();
        for (Product p : products) {
            Object[] rowData = {
                    p.getId(),
                    p.getName(),
                    p.getCategoryName(),
                    p.getQuantity(),
                    p.getPrice()
            };
            tableModel.addRow(rowData);
        }

        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton btnSearch = new JButton("Αναζήτηση");

        searchPanel.add(new JLabel("Όνομα Προϊόντος: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);

        frame.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Προσθήκη");
        JButton btnEdit = new JButton("Επεξεργασία");
        JButton btnDelete = new JButton("Διαγραφή");
        JButton btnExport = new JButton("Εξαγωγή CSV");
        JButton btnImport = new JButton("Εισαγωγή CSV");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnImport);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField quantityField = new JTextField();
            JTextField priceField = new JTextField();

            String[] categories = {"1 - Hardware",
                    "2 - Περιφερειακά",
                    "3 - Δικτυακός Εξοπλισμός",
                    "4 - Αποθηκευτικά Μέσα",
                    "5 - Λογισμικό"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);

            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.add(new JLabel("Όνομα προϊόντος: "));
            panel.add(nameField);
            panel.add(new JLabel("Κατηγορία: "));
            panel.add(categoryCombo);
            panel.add(new JLabel("Ποσότητα: "));
            panel.add(quantityField);
            panel.add(new JLabel("Τιμή (€): "));
            panel.add(priceField);

            int result = JOptionPane.showConfirmDialog(frame, panel,
                    "Προσθήκη Νέου Προϊόντος", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String quantityStr = quantityField.getText().trim();
                String priceStr = priceField.getText().trim();

                if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Όλα τα πεδία είναι υποχρεωτικά! Παρακαλώ συμπληρώστε τα.",
                            "Κενά Δεδομένα", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityStr);
                    double price = Double.parseDouble(priceStr);

                    String selectedCat = (String) categoryCombo.getSelectedItem();
                    int categoryId = Integer.parseInt(selectedCat.substring(0, 1));

                    db.addProduct(name, quantity, price, categoryId);

                    btnSearch.doClick();

                    JOptionPane.showMessageDialog(frame,
                            "Το προϊόν προστέθηκε επιτυχώς!",
                            "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Σφάλμα: Η ποσότητα και η τιμή πρέπει να είναι αριθμοί!",
                            "Λάθος Εισαγωγή", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame,
                        "Παρακαλώ επιλέξτε ένα προϊόν από τον πίνακα για επεξεργασία!",
                        "Προσοχή", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int productId = (int) tableModel.getValueAt(selectedRow, 0);

            Product product = db.getProductById(productId);
            if (product == null) {
                JOptionPane.showMessageDialog(frame,
                        "Σφάλμα: Το προϊόν δεν βρέθηκε στην βάση!",
                        "Λάθος", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JTextField nameField = new JTextField(product.getName());
            JTextField quantityField = new JTextField(String.valueOf(product.getQuantity()));
            JTextField priceField = new JTextField(String.valueOf(product.getPrice()));

            String[] categories = {"1 - Hardware",
                    "2 - Περιφερειακά",
                    "3 - Δικτυακός Εξοπλισμός",
                    "4 - Αποθηκευτικά Μέσα",
                    "5 - Λογισμικό"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);

            String catName = product.getCategoryName();
            if (catName.equals("Hardware")) categoryCombo.setSelectedIndex(0);
            else if (catName.equals("Περιφερειακά")) categoryCombo.setSelectedIndex(1);
            else if (catName.equals("Δικτυακός Εξοπλισμός")) categoryCombo.setSelectedIndex(2);
            else if (catName.equals("Αποθηκευτικά Μέσα")) categoryCombo.setSelectedIndex(3);
            else if (catName.equals("Λογισμικό")) categoryCombo.setSelectedIndex(4);

            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.add(new JLabel("Όνομα Προϊόντος:"));
            panel.add(nameField);
            panel.add(new JLabel("Κατηγορία:"));
            panel.add(categoryCombo);
            panel.add(new JLabel("Ποσότητα:"));
            panel.add(quantityField);
            panel.add(new JLabel("Τιμή (€):"));
            panel.add(priceField);

            int result = JOptionPane.showConfirmDialog(frame, panel,
                    "Επεξεργασία Προϊόντος (ID: " + productId + ")", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String quantityStr = quantityField.getText().trim();
                String priceStr = priceField.getText().trim();

                if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Όλα τα πεδία είναι υποχρεωτικά! Οι αλλαγές ακυρώθηκαν.",
                            "Κενά Δεδομένα", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int newQuantity = Integer.parseInt(quantityStr);
                    double newPrice = Double.parseDouble(priceStr);

                    String selectedCat = (String) categoryCombo.getSelectedItem();
                    int newCategoryId = Integer.parseInt(selectedCat.substring(0, 1));

                    db.updateProduct(productId, name, newQuantity, newPrice, newCategoryId);

                    btnSearch.doClick();

                    JOptionPane.showMessageDialog(frame,
                            "Το προϊόν ενημερώθηκε επιτυχώς!",
                            "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Σφάλμα: Η ποσότητα και η τιμή πρέπει να είναι αριθμοί!",
                            "Λάθος Εισαγωγή", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();

            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Επίλεξτε τουλάχιστον ένα προϊόν από τον πίνακα πρώτα!",
                        "Προσοχή", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String message = (selectedRows.length == 1)
                    ? "Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο προϊόν;"
                    : "Είστε σίγουροι ότι θέλετε να διαγράψετε τα " + selectedRows.length + " επιλεγμένα προϊόντα;";

            int confirm = JOptionPane.showConfirmDialog(frame, message,
                    "Επιβεβαίωση Διαγραφής", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                for (int i = 0; i < selectedRows.length; i++) {
                    int productId = (int) table.getValueAt(selectedRows[i], 0);
                    db.deleteProduct(productId);
                }

                btnSearch.doClick();

                JOptionPane.showMessageDialog(frame,
                        "Η διαγραφή ολοκληρώθηκε επιτυχώς!");
            }
        });

        btnSearch.addActionListener(e -> {
            String keyword = searchField.getText();
            List<Product> results = db.searchProducts(keyword);

            tableModel.setRowCount(0);

            for (Product p : results) {
                Object[] rowData = {
                        p.getId(),
                        p.getName(),
                        p.getCategoryName(),
                        p.getQuantity(),
                        p.getPrice()
                };
                tableModel.addRow(rowData);
            }
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String keyword = searchField.getText();
                List<Product> results = db.searchProducts(keyword);

                tableModel.setRowCount(0);
                for (Product p : results) {
                    Object[] rowData = {
                            p.getId(),
                            p.getName(),
                            p.getCategoryName(),
                            p.getQuantity(),
                            p.getPrice()
                    };
                    tableModel.addRow(rowData);
                }
            }
        });

        btnExport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Αποθήκευση αρχείου CSV");

            int userSelection = fileChooser.showSaveDialog(frame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filepath = fileChooser.getSelectedFile().getAbsolutePath();

                if (!filepath.endsWith(".csv")) {
                    filepath += ".csv";
                }

                db.exportToCSV(filepath);
                JOptionPane.showMessageDialog(frame,
                        "Η εξαγωγή ολοκληρώθηκε στο:\n" + filepath,
                        "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Επιλέξτε αρχείο CSV για Εισαγωγή");

            int usrSelection = fileChooser.showOpenDialog(frame);

            if (usrSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();

                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line;
                    Boolean isHeader = true;
                    int importedCount = 0;

                    while ((line = br.readLine()) != null) {
                        if (isHeader) {
                            isHeader = false;
                            continue;
                        }

                        String[] parts = parseCSVLine(line);
                        if (parts.length >= 5) {
                            String name = parts[1].trim();
                            String catName = parts[2].trim();
                            int quantity = Integer.parseInt(parts[3].trim());

                            String priceStr = parts[4].replace("€", "").trim();
                            double price = Double.parseDouble(priceStr);

                            int categoryId = 1;
                            if (catName.equalsIgnoreCase("Hardware")) categoryId = 1;
                            else if (catName.equalsIgnoreCase("Περιφερειακά")) categoryId = 2;
                            else if (catName.equalsIgnoreCase("Δικτυακός Εξοπλισμός")) categoryId = 3;
                            else if (catName.equalsIgnoreCase("Αποθηκευτικά Μέσα")) categoryId = 4;
                            else if (catName.equalsIgnoreCase("Λογισμικό")) categoryId = 5;

                            db.addProduct(name, quantity, price, categoryId);
                            importedCount++;
                        }
                    }

                    btnSearch.doClick();
                    JOptionPane.showMessageDialog(frame,
                            "Εισηχθήσαν επιτυχώς " + importedCount + " προϊόντα στη βάση!",
                            "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Σφάλμα κατά την ανάγνωση του αρχείου: " + ex.getMessage(),
                            "Λάθος Αρχείο", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++){
            char c = line.charAt(i);

            if (inQuotes) {
               if (c == '"') {
                   if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                       current.append('"');
                       i++;
                   } else {
                       inQuotes = false;
                   }
               } else {
                   current.append(c);
               }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}