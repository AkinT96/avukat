package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VerfahrenDAO {

    public static void verfahrenHinzufuegen(int kiraciId, Date datum, String beschreibung, String status) {
        String sql = "INSERT INTO immobilienverfahren (kiraci_id, datum, beschreibung, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            stmt.setDate(2, datum);
            stmt.setString(3, beschreibung);
            stmt.setString(4, status);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getVerfahrenFuerKiraci(int kiraciId) {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT datum, beschreibung, status FROM immobilienverfahren WHERE kiraci_id = ? ORDER BY datum DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String datum = rs.getDate("datum").toString();
                String beschreibung = rs.getString("beschreibung");
                String status = rs.getString("status");
                liste.add(new String[]{datum, beschreibung, status});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return liste;
    }

}
