package UI;

import db.KiraciDAO;
import db.ImmobilieDAO;
import model.Kiraci;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Hauptfenster extends JFrame {

    public Hauptfenster() {
        setTitle("BulKira"); // Fenstertitel (Pencere başlığı)
        setSize(800, 600); // Fenstergröße (Pencere boyutu)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beim Schließen beenden (Kapatınca çık)
        setLocationRelativeTo(null); // In der Mitte anzeigen (Ortada göster)

        initGUI(); // Oberfläche initialisieren (Arayüzü başlat)

        setVisible(true); // Fenster sichtbar machen (Pencereyi görünür yap)
    }

    private void initGUI() {
        // Vor dem GUI alle Mieter durchgehen und automatische Mieten eintragen
        List<Kiraci> alle = KiraciDAO.getAlleKiracis();
        for (Kiraci k : alle) {
            ImmobilieDAO.pruefeUndTrageMieteEin(k.getId());
            ImmobilieDAO.pruefeUndTrageFehlendeMietenNach(k.getId()); // <- Hier einfügen

            // Guthaben basierend auf "Bekliyor"-Zahlungen berechnen
            double bakiye = db.ZahlungDAO.getOffenesGuthaben(k.getId());

            // Optional: Ergebnis auch im Terminal anzeigen (nur zur Kontrolle)
            System.out.println(k.getVorname() + " " + k.getNachname() + " → Bakiye: " + bakiye + " ₺");
        }

        JTabbedPane tabs = new JTabbedPane(); // Tab-Leiste (Sekme çubuğu)
        tabs.addTab("Kiracılar", new KiraciVerwaltung()); // Kiracı-Verwaltung anzeigen (Kiracı yönetimini göster)

        add(tabs); // Tabs dem Fenster hinzufügen (Sekmeleri pencereye ekle)
    }
}
