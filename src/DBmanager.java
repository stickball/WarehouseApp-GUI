import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class DBmanager {
    private String URL;
    private String USER;
    private String PASSWORD;

    public DBmanager(String host, String dbName, String username, String password) {
        this.URL = "jdbc:mysql://" + host + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        this.USER = username;
        this.PASSWORD = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //προσθήκη προϊόντος
    public void addProduct(String name, int quantity, double price, int categoryId) {
        String query = "INSERT INTO products (name, quantity, price, category_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, categoryId);
            pstmt.executeUpdate();
            System.out.println("Το προϊόν προστέθηκε επιτυχώς!");

            } catch (SQLException e) {
                System.out.println("Σφάλμα κατά την προσθήκη: " + e.getMessage());
        }
    }

    //προβολή προϊόντων
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String query = "SELECT products.*, categories.name AS category_name " +
                    "FROM products " +
                    "LEFT JOIN categories ON products.category_id = categories.id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String catName = rs.getString("category_name");
                if (catName == null) catName = "Χωρίς Κατηγορία";

                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        catName
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Σφάλμα κατά την ανάγνωση: " + e.getMessage());
        }
        return products;
    }

    //ενημέρωση προϊόντος
    public void updateProduct(int id, String name, int quantity, double price, int categoryId) {
        String query = "UPDATE products SET name = ?, quantity = ?, price = ?, category_id = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, categoryId);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
            System.out.println("Το προϊόν ενημερώθηκε επιτυχώς!");
        } catch (SQLException e) {
            System.out.println("Σφάλμα κατά την ενημέρωση: " + e.getMessage());
        }
    }

    //διαγραφή προϊόντος
    public void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Το προϊόν διαγράφηκε επιτυχώς!");

        } catch (SQLException e) {
            System.out.println("Σφάλμα κατά την διαγραφή: " + e.getMessage());
        }
    }

    // εύρεση προϊόντος με το ID (για τις παλιές τιμές στο Update)
    public Product getProductById(int id) {
        String query = "SELECT products.*, categories.name AS category_name " +
                    "FROM products " +
                    "LEFT JOIN categories ON products.category_id = categories.id " +
                    "WHERE products.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String catName = rs.getString("category_name");
                    if (catName == null) catName = "Χωρίς Κατηγορία";

                    return new Product (
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            catName
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Σφαλμα κατά την εύρεση προϊόντος: " + e.getMessage());
        }
        return null;
    }

    //αναζητηση προϊόντων
    public List<Product> searchProducts(String keyword){
        List<Product> products = new ArrayList<>();

        String query = "SELECT products.*, categories.name AS category_name " +
                    "FROM products " +
                    "LEFT JOIN categories ON products.category_id = categories.id " +
                    "WHERE products.name LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");

            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    String catName = rs.getString("category_name");
                    if (catName == null) catName = "Χωρίς Κατηγορία";

                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            catName
                    );
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.out.println("Σφάλμα κατά την αναζήτηση: " + e.getMessage());
        }
        return products;
    }

    //γράψιμο αρχείου (εξαγωγή)
    public void exportToCSV(String filename) {
        String query = "SELECT products.*, categories.name AS category_name " +
                    "FROM products " +
                    "LEFT JOIN categories ON products.category_id = categories.id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter writer = new FileWriter(filename)) {

            writer.append("ID,Όνομα,Κατηγορία,Ποσότητα,Τιμή\n");

            int count = 0;
            while (rs.next()) {
                String catName = rs.getString("category_name");
                if (catName == null) catName = "Χωρίς Κατηγορία";

                writer.append(rs.getInt("id") + ",");
                writer.append(rs.getString("name") + ",");
                writer.append(catName + ",");
                writer.append(rs.getInt("quantity") + ",");
                writer.append(rs.getDouble("price") + "\n");
                count++;
            }

            System.out.println("Εξήχθησαν " + count + " προϊόντα στο αρχείο: " + filename);
        } catch (SQLException | IOException e) {
            System.out.println("Σφάλμα κατά την εξαγωγή: " + e.getMessage());
        }

    }
}
