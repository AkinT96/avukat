package db;

import model.Kiraci;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KiraciDAO {

    // Alle Kiracı aus der Datenbank laden (Veritabanından tüm kiracıları al)
    public static List<Kiraci> getAlleKiracis() {
        List<Kiraci> liste = new ArrayList<>();
        String sql = "SELECT * FROM kiraci";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Kiraci k = new Kiraci(
                        rs.getInt("id"),
                        rs.getString("vorname"),
                        rs.getString("nachname"),
                        rs.getString("adresse"),
                        rs.getString("vermieter"),
                        rs.getDouble("guthaben")
                );
                liste.add(k);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return liste;
    }

    // Einen Kiracı hinzufügen (Yeni bir kiracı ekle)
    public static void kiraciHinzufuegen(Kiraci k) {
        String sql = "INSERT INTO kiraci (vorname, nachname, adresse, vermieter, guthaben) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, k.getVorname());
            stmt.setString(2, k.getNachname());
            stmt.setString(3, k.getAdresse());
            stmt.setString(4, k.getVermieter());
            stmt.setDouble(5, k.getGuthaben());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Einen Kiracı löschen über Vor- und Nachname (isimle sil)
    public static void kiraciLoeschen(String vorname, String nachname) {
        String sql = "DELETE FROM kiraci WHERE vorname = ? AND nachname = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vorname);
            stmt.setString(2, nachname);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Kiracı nach ID löschen (ID ile sil)
    public static void kiraciLoeschen(int id) {
        String sql = "DELETE FROM kiraci WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ID anhand von Vor- und Nachname ermitteln (Ad/Soyad'a göre ID getir)
    public static int getIdByName(String vorname, String nachname) {
        String sql = "SELECT id FROM kiraci WHERE vorname = ? AND nachname = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vorname);
            stmt.setString(2, nachname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // nicht gefunden
    }
}
