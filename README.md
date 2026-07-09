# WarehouseApp - GUI

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)

Μια εφαρμογή σε Java (με Γραφικό Περιβάλλον - GUI) για τη διαχείριση μιας αποθήκης προϊόντων.
Το project αποτελεί την οπτική εξέλιξη της αρχικής μου [εφαρμογής κονσόλας](https://github.com/stickball/WarehouseApp-CLI) και δημιουργήθηκε με σκοπό την πρακτική εξάσκηση στο Java Swing, την ενσωμάτωση εξωτερικών βιβλιοθηκών (FlatLaf) και τη δημιουργία δυναμικών συνδέσεων με MySQL (JDBC).

## Τι περιλαμβάνει
* **Διαχείριση Προϊόντων (CRUD):** Πλήρες γραφικό περιβάλλον για προσθήκη, προβολή, επεξεργασία και διαγραφή.
* **Μαζικές Ενέργειες:** Επιλογή πολλών προϊόντων μαζί (Shift/Ctrl) για ταυτόχρονη διαγραφή.
* **Live Αναζήτηση:** Άμεσο φιλτράρισμα του πίνακα προϊόντων καθώς πληκτρολογείς.
* **Εισαγωγή & Εξαγωγή CSV:** Δυνατότητα μαζικής εισαγωγής προϊόντων ή εξαγωγής όλης της αποθήκης σε αρχείο `.csv` (Excel).
* **Οθόνη Login:** Δυναμικό παράθυρο σύνδεσης στην εκκίνηση για τα στοιχεία του MySQL Server (IP, DB Name, Username, Password).
* **Μοντέρνο UI:** Σύγχρονο περιβάλλον με Dark Mode μέσω της βιβλιοθήκης FlatLaf.

## Πώς να το τρέξεις τοπικά
1. Άνοιξε το WampServer (ή XAMPP) και βεβαιώσου ότι η MySQL τρέχει.
2. Φτιάξε μια βάση δεδομένων (π.χ. με όνομα `warehouse_db`) στο phpMyAdmin.
3. Τρέξε το παρακάτω SQL query για να δημιουργηθούν οι πίνακες και οι αρχικές κατηγορίες:

```sql
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(25) NOT NULL
);

INSERT INTO categories (name) VALUES
('Hardware'),
('Περιφερειακά'),
('Δικτυακός Εξοπλισμός'),
('Αποθηκευτικά Μέσα'),
('Λογισμικό');

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```
4. Πήγαινε στα Releases και κατέβασε το αρχείο WarehouseApp.exe.
5. Τρέξε το .exe, βάλε τα στοιχεία της βάσης σου στο παράθυρο Login που θα εμφανιστεί και είσαι έτοιμος! (Αν θέλεις να δεις τον κώδικα, άνοιξε τον φάκελο src στο IDE σου. Οι απαραίτητες βιβλιοθήκες βρίσκονται στον φάκελο lib).
