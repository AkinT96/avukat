package db;

import model.Mandant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MandantDAO {

    public static List<Mandant> getAlleMandanten() {
        List<Mandant> mandantenListe = new ArrayList<>(); // Liste aller Mandanten (Tüm müşterilerin listesi)

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mandant")) {

            while (rs.next()) {
                Mandant m = new Mandant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("anschrift"),
                        rs.getString("telefon")
                );
                mandantenListe.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerausgabe (Hata çıktısı)
        }

        return mandantenListe;
    }

    public static void mandantHinzufuegen(Mandant mandant) {
        String sql = "INSERT INTO mandant (name, anschrift, telefon) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mandant.getName());
            pstmt.setString(2, mandant.getAnschrift());
            pstmt.setString(3, mandant.getTelefon());

            pstmt.executeUpdate(); // Datensatz einfügen (Veriyi ekle)

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void mandantLoeschen(int id) {
        String sql = "DELETE FROM mandant WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate(); // Datensatz löschen (Veriyi sil)

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
