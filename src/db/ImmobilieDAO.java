package db;

import java.sql.*;

public class ImmobilieDAO {

    public static void mieteUndKautionEintragen(int kiraciId, double miete, double kaution, Date zahlungstag) {
        String sql = "INSERT INTO immobilie (kiraci_id, miete, kaution, zahlungstag) VALUES (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE miete = VALUES(miete), kaution = VALUES(kaution), zahlungstag = VALUES(zahlungstag)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            stmt.setDouble(2, miete);
            stmt.setDouble(3, kaution);
            stmt.setDate(4, zahlungstag);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getMiete(int kiraciId) {
        String sql = "SELECT miete FROM immobilie WHERE kiraci_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("miete");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date getZahlungstag(int kiraciId) {
        String sql = "SELECT zahlungstag FROM immobilie WHERE kiraci_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDate("zahlungstag");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hatBereitsAutomatischEingetragenHeute(int kiraciId) {
        String sql = "SELECT COUNT(*) FROM zahlung WHERE kiraci_id = ? AND datum = WHERE datum = ? AND vermerk = 'Otomatik Kira'\n AND vermerk = 'Otomatik Kira'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void pruefeUndTrageFehlendeMietenNach(int kiraciId) {
        Date zahlungstag = getZahlungstag(kiraciId);
        double miete = getMiete(kiraciId);

        if (zahlungstag == null || miete <= 0) return;

        java.time.LocalDate start = getLetztesAutomatischesDatum(kiraciId);
        if (start == null) {
            // Wenn noch nie automatisch eingetragen wurde, beginne mit dem ersten gültigen Zahlungstag
            start = java.time.LocalDate.of(java.time.LocalDate.now().getYear(), java.time.LocalDate.now().getMonthValue(), zahlungstag.toLocalDate().getDayOfMonth());
        }

        java.time.LocalDate heute = java.time.LocalDate.now();
        java.time.LocalDate aktuellesDatum = start;

        while (aktuellesDatum.plusMonths(1).withDayOfMonth(zahlungstag.toLocalDate().getDayOfMonth()).isBefore(heute)) {
            aktuellesDatum = aktuellesDatum.plusMonths(1).withDayOfMonth(zahlungstag.toLocalDate().getDayOfMonth());

            if (!hatAutomatischeZahlungAmTag(kiraciId, java.sql.Date.valueOf(aktuellesDatum))) {
                model.Zahlung z = new model.Zahlung(
                        kiraciId,
                        java.sql.Date.valueOf(aktuellesDatum),
                        -miete,
                        "Bekliyor",
                        "Otomatik Kira"
                );
                ZahlungDAO.zahlungHinzufuegen(z);
            }
        }
    }

    private static java.time.LocalDate getLetztesAutomatischesDatum(int kiraciId) {
        String sql = "SELECT MAX(datum) FROM zahlung WHERE kiraci_id = ? AND vermerk = 'Otomatik Kira'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getDate(1) != null) {
                return rs.getDate(1).toLocalDate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean hatAutomatischeZahlungAmTag(int kiraciId, Date datum) {
        String sql = "SELECT COUNT(*) FROM zahlung WHERE kiraci_id = ? AND datum = ? AND vermerk = 'Otomatik Kira'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            stmt.setDate(2, datum);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void pruefeUndTrageMieteEin(int kiraciId) {
        Date ersterZahlungstag = getZahlungstag(kiraciId);
        double miete = getMiete(kiraciId);

        if (ersterZahlungstag != null && miete > 0) {
            java.time.LocalDate start = ersterZahlungstag.toLocalDate();
            java.time.LocalDate heute = java.time.LocalDate.now();

            while (!start.isAfter(heute)) {
                if (!hatAutomatischeMieteFürDatum(kiraciId, Date.valueOf(start))) {
                    model.Zahlung z = new model.Zahlung(
                            kiraciId,
                            Date.valueOf(start),
                            -miete,
                            "Bekliyor",
                            "Otomatik Kira"
                    );
                    ZahlungDAO.zahlungHinzufuegen(z);
                }
                start = start.plusMonths(1); // gleicher Tag im Folgemonat
            }
        }
    }

    public static double getKaution(int kiraciId) {
        String sql = "SELECT kaution FROM immobilie WHERE kiraci_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kiraciId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("kaution");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean hatAutomatischeMieteFürDatum(int kiraciId, Date datum) {
        String sql = "SELECT COUNT(*) FROM zahlung WHERE kiraci_id = ? AND datum = ? AND vermerk = 'Otomatik Kira'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kiraciId);
            stmt.setDate(2, datum);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
