package db;

import model.Zahlung;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ZahlungDAO {

    public static List<Zahlung> getZahlungenFuerKiraci(int kiraciId) {
        List<Zahlung> zahlungen = new ArrayList<>();
        String sql = "SELECT * FROM zahlung WHERE kiraci_id = ? ORDER BY datum DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Zahlung z = new Zahlung(
                        rs.getInt("id"),
                        rs.getInt("kiraci_id"),
                        rs.getDate("datum"),
                        rs.getDouble("betrag"),
                        rs.getString("typ"),
                        rs.getString("vermerk")
                );
                zahlungen.add(z);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zahlungen;
    }

    public static void exportZahlungenAlsCSV(int kiraciId, File zielDatei) {
        List<Zahlung> zahlungen = getZahlungenFuerKiraci(kiraciId);
        try (PrintWriter writer = new PrintWriter(zielDatei, "UTF-8")) {
            writer.println("Datum,Betrag,Typ,Vermerk");
            for (Zahlung z : zahlungen) {
                writer.printf("%s,%.2f,%s,%s%n", z.getDatum(), z.getBetrag(), z.getTyp(), z.getVermerk());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zahlungHinzufuegen(Zahlung z) {
        String sql = "INSERT INTO zahlung (kiraci_id, datum, betrag, typ, vermerk) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, z.getKiraciId());
            stmt.setDate(2, z.getDatum());
            stmt.setDouble(3, z.getBetrag());
            stmt.setString(4, z.getTyp());
            stmt.setString(5, z.getVermerk());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void zahlungLoeschen(int zahlungId) {
        String sql = "DELETE FROM zahlung WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, zahlungId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void zahlungAlsBezahltMarkieren(Zahlung z) {
        String sql = "UPDATE zahlung SET typ = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, z.getTyp()); // z. B. "Ödendi"
            stmt.setInt(2, z.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getOffenesGuthaben(int kiraciId) {
        String sql = "SELECT SUM(betrag) FROM zahlung WHERE kiraci_id = ? AND typ = 'Bekliyor'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
