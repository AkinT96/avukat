package UI;

import db.ImmobilieDAO;
import db.KiraciDAO;
import db.ZahlungDAO;
import model.Kiraci;
import model.Zahlung;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class KiraciVerwaltung extends JPanel {

    private JTable tabelle;
    private DefaultTableModel model;

    public KiraciVerwaltung() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"Ad", "Soyad", "Adres", "Ev sahibi", "Bakiye"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelle = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabelle);

        JButton hinzufuegenButton = new JButton("Kiracı ekle");
        JButton loeschenButton = new JButton("Kiracı sil");
        JButton detailsButton = new JButton("Detayları göster");

        hinzufuegenButton.addActionListener(e -> kiraciHinzufuegen());
        loeschenButton.addActionListener(e -> kiraciLoeschen());
        detailsButton.addActionListener(e -> kiraciDetailsAnzeigen());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(hinzufuegenButton);
        buttonPanel.add(loeschenButton);
        buttonPanel.add(detailsButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel suchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField suchFeld = new JTextField(20);
        JButton suchButton = new JButton("Ara");

        suchButton.addActionListener(e -> sucheKiraci(suchFeld.getText()));
        suchFeld.addActionListener(e -> sucheKiraci(suchFeld.getText())); // Enter gedrückt

        suchPanel.add(new JLabel("Ara:"));
        suchPanel.add(suchFeld);
        suchPanel.add(suchButton);

        add(suchPanel, BorderLayout.NORTH);

        kiracilariLaden();
    }

    private void sucheKiraci(String begriff) {
        model.setRowCount(0);
        List<Kiraci> liste = KiraciDAO.getAlleKiracis();

        for (Kiraci k : liste) {
            if (k.getVorname().toLowerCase().contains(begriff.toLowerCase()) ||
                    k.getNachname().toLowerCase().contains(begriff.toLowerCase()) ||
                    k.getAdresse().toLowerCase().contains(begriff.toLowerCase()) ||
                    (k.getVermieter() != null && k.getVermieter().toLowerCase().contains(begriff.toLowerCase()))) {

                double bakiye = berechneGuthaben(k.getId());
                Object[] zeile = new Object[]{
                        k.getVorname(),
                        k.getNachname(),
                        k.getAdresse(),
                        k.getVermieter(),
                        String.format("%.2f ₺", bakiye)
                };
                model.addRow(zeile);
            }
        }
    }


    private void kiracilariLaden() {
        model.setRowCount(0); // Tabelle leeren
        List<Kiraci> liste = KiraciDAO.getAlleKiracis();

        for (Kiraci k : liste) {
            ImmobilieDAO.pruefeUndTrageMieteEin(k.getId()); // Automatische Miete prüfen
            double bakiye = berechneGuthaben(k.getId());
            Object[] zeile = new Object[]{
                    k.getVorname(),
                    k.getNachname(),
                    k.getAdresse(),
                    k.getVermieter(),
                    String.format("%.2f ₺", bakiye)
            };
            model.addRow(zeile);
        }

        // Farbliche Hervorhebung (grün/rot) für Bakiye
        tabelle.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 4) { // Bakiye-Spalte
                    String val = value.toString().replace("₺", "").trim().replace(",", ".");
                    double betrag = Double.parseDouble(val);
                    c.setForeground(betrag < 0 ? Color.RED : new Color(0, 128, 0));
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
    }

    private double berechneGuthaben(int kiraciId) {
        List<Zahlung> zahlungen = ZahlungDAO.getZahlungenFuerKiraci(kiraciId);
        return zahlungen.stream()
                .filter(z -> "Bekliyor".equals(z.getTyp()))
                .mapToDouble(Zahlung::getBetrag)
                .sum();
    }


    private void kiraciHinzufuegen() {
        JTextField vorname = new JTextField();
        JTextField nachname = new JTextField();
        JTextField adresse = new JTextField();
        JTextField vermieter = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Ad:"));
        panel.add(vorname);
        panel.add(new JLabel("Soyad:"));
        panel.add(nachname);
        panel.add(new JLabel("Adres:"));
        panel.add(adresse);
        panel.add(new JLabel("Ev sahibi (opsiyonel):"));
        panel.add(vermieter);

        int result = JOptionPane.showConfirmDialog(this, panel, "Yeni Kiracı", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Kiraci k = new Kiraci(vorname.getText(), nachname.getText(), adresse.getText(), vermieter.getText());
            KiraciDAO.kiraciHinzufuegen(k);
            kiracilariLaden();
        }
    }

    private void kiraciLoeschen() {
        int zeile = tabelle.getSelectedRow();
        if (zeile >= 0) {
            String vorname = (String) model.getValueAt(zeile, 0);
            String nachname = (String) model.getValueAt(zeile, 1);

            int bestaetigung = JOptionPane.showConfirmDialog(
                    this,
                    vorname + " " + nachname + " silinsin mi?",
                    "Silme onayı",
                    JOptionPane.YES_NO_OPTION
            );

            if (bestaetigung == JOptionPane.YES_OPTION) {
                int id = KiraciDAO.getIdByName(vorname, nachname);
                KiraciDAO.kiraciLoeschen(id);
                kiracilariLaden();
            }
        }
    }

    private void kiraciDetailsAnzeigen() {
        int zeile = tabelle.getSelectedRow();
        if (zeile >= 0) {
            String vorname = (String) model.getValueAt(zeile, 0);
            String nachname = (String) model.getValueAt(zeile, 1);
            String adresse = (String) model.getValueAt(zeile, 2);
            String vermieter = (String) model.getValueAt(zeile, 3);

            int id = KiraciDAO.getIdByName(vorname, nachname);
            Kiraci k = new Kiraci(id, vorname, nachname, adresse, vermieter, 0); // Bakiye später berechnet
            KiraciDetailsFenster fenster = new KiraciDetailsFenster(k);
            fenster.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    kiracilariLaden(); // Liste aktualisieren
                }
            });
        }
    }
}
