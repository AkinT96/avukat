package UI;

import db.ImmobilieDAO;
import db.ZahlungDAO;
import model.Kiraci;
import model.Zahlung;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class KiraciDetailsFenster extends JFrame {

    private JLabel guthabenLabel;
    private DefaultTableModel zahlungsModel;
    private Kiraci kiraci;
    private JTable zahlungstabelle;
    private JButton alsBezahltButton;
    private JButton zahlungLoeschenButton;
    private DefaultTableModel verfahrenModel;
    private JTable verfahrenTabelle;
    private JLabel adresseLabel;
    private JLabel mieteLabel = new JLabel("Kira: Girilmemiş");
    private JLabel kautionLabel = new JLabel("Depozito: Girilmemiş");
    private static String globalerSpeicherPfad = null;


    public KiraciDetailsFenster(Kiraci kiraci) {
        this.kiraci = kiraci;
        if (ImmobilieDAO.getMiete(kiraci.getId()) > 0) {
            mieteLabel.setText("Kira: " + ImmobilieDAO.getMiete(kiraci.getId()) + "₺");
        }
        if (ImmobilieDAO.getKaution(kiraci.getId()) > 0) {
            kautionLabel.setText("Depozito: " + ImmobilieDAO.getKaution(kiraci.getId()) + "₺");
        }


        setTitle(kiraci.getVorname() + " - " + kiraci.getNachname());
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Genel Bilgiler
        JPanel allgemeinPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        allgemeinPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        allgemeinPanel.add(new JLabel("Ad:"));
        allgemeinPanel.add(new JLabel(kiraci.getVorname()));

        allgemeinPanel.add(new JLabel("Soyad:"));
        allgemeinPanel.add(new JLabel(kiraci.getNachname()));

        allgemeinPanel.add(new JLabel("Adres:"));
        allgemeinPanel.add(new JLabel(kiraci.getAdresse()));

        allgemeinPanel.add(new JLabel("Ev sahibi:"));
        allgemeinPanel.add(new JLabel(kiraci.getVermieter()));

        allgemeinPanel.add(new JLabel("Bakiye:"));
        guthabenLabel = new JLabel();
        allgemeinPanel.add(guthabenLabel);

        tabs.addTab("Genel", allgemeinPanel);

        // Tab 2: Ödemeler
        JPanel zahlungenPanel = new JPanel(new BorderLayout());

        zahlungsModel = new DefaultTableModel(new Object[]{"Tarih", "Tutar", "Durum", "Açıklama"}, 0);
        zahlungstabelle = new JTable(zahlungsModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String typ = (String) getValueAt(row, 2); // Spalte "Durum"

                if ("Bekliyor".equals(typ)) {
                    c.setBackground(new Color(255, 230, 230)); // Hellrot
                } else if ("Ödendi".equals(typ)) {
                    c.setBackground(new Color(220, 255, 220)); // Hellgrün
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        };


        zahlungstabelle.setFillsViewportHeight(true);
        zahlungenPanel.add(new JScrollPane(zahlungstabelle), BorderLayout.CENTER);
        JButton exportButton = new JButton("CSV Dışa Aktar");
        exportButton.addActionListener(e -> exportiereZahlungenAlsCSV());


        JButton zahlungHinzufuegen = new JButton("Ödeme ekle");
        zahlungHinzufuegen.addActionListener(e -> zahlungHinzufuegen());

        // Gemeinsames Panel für alle Buttons unten
        JPanel buttonBereich = new JPanel(new BorderLayout());

// Linke Seite: CSV-Export
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportPanel.add(exportButton);

// Rechte Seite: Hinzufügen, Bezahlt, Löschen
        JPanel aktionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        alsBezahltButton = new JButton("Ödendi olarak işaretle");
        alsBezahltButton.setVisible(false);
        alsBezahltButton.addActionListener(e -> zahlungAlsBezahltMarkieren());

        zahlungLoeschenButton = new JButton("Ödemeyi sil");
        zahlungLoeschenButton.setVisible(false);
        zahlungLoeschenButton.addActionListener(e -> zahlungLoeschen());

        aktionPanel.add(zahlungHinzufuegen);
        aktionPanel.add(alsBezahltButton);
        aktionPanel.add(zahlungLoeschenButton);

        buttonBereich.add(exportPanel, BorderLayout.WEST);
        buttonBereich.add(aktionPanel, BorderLayout.EAST);

// Unten einfügen
        zahlungenPanel.add(buttonBereich, BorderLayout.SOUTH);
        zahlungstabelle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = zahlungstabelle.getSelectedRow();
                if (selectedRow >= 0) {
                    String typ = (String) zahlungsModel.getValueAt(selectedRow, 2);
                    alsBezahltButton.setVisible("Bekliyor".equals(typ));
                    zahlungLoeschenButton.setVisible(true);
                } else {
                    alsBezahltButton.setVisible(false);
                    zahlungLoeschenButton.setVisible(false);
                }
            }
        });

        tabs.addTab("Ödemeler", zahlungenPanel);

        // Tab 3: Taşınmaz
        JPanel immobiliePanel = new JPanel();
        immobiliePanel.setLayout(new BoxLayout(immobiliePanel, BoxLayout.Y_AXIS));
        immobiliePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel adresseLabel = new JLabel("Taşınmaz Adresi: " + kiraci.getAdresse());
        immobiliePanel.add(adresseLabel);
        immobiliePanel.add(Box.createVerticalStrut(10));
        immobiliePanel.add(mieteLabel);
        immobiliePanel.add(kautionLabel);

        immobiliePanel.add(Box.createVerticalStrut(20));

        verfahrenModel = new DefaultTableModel(new Object[]{"Tarih", "İşlem", "Durum"}, 0);
        verfahrenTabelle = new JTable(verfahrenModel);
        JScrollPane scrollPane = new JScrollPane(verfahrenTabelle);
        immobiliePanel.add(scrollPane);

        JPanel immobilieButtonPanel = new JPanel(new FlowLayout());
        JButton mietdatenEintragen = new JButton("Kira/Depozito ekle");
        mietdatenEintragen.addActionListener(e -> kiraDepozitoEingeben(kiraci.getId()));

        JButton verfahrenHinzufuegen = new JButton("İşlem ekle");
        verfahrenHinzufuegen.addActionListener(e -> verfahrenHinzufuegen(kiraci.getId()));

        JButton verfahrenOeffnen = new JButton("İşlem Aç");
        verfahrenOeffnen.addActionListener(e -> islemOeffnen());

        immobilieButtonPanel.add(verfahrenOeffnen);


        immobilieButtonPanel.add(mietdatenEintragen);
        immobilieButtonPanel.add(verfahrenHinzufuegen);
        immobiliePanel.add(immobilieButtonPanel);

        tabs.addTab("Taşınmaz", immobiliePanel);

        add(tabs);
        ladeVerfahrenTabelle(kiraci.getId());

        zahlungenLadenUndGuthabenBerechnen();
        setVisible(true);
    }

    private void exportiereZahlungenAlsCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("CSV dosyasını kaydet");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Dosyaları", "csv"));

        int userSelection = chooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();

            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {
                writer.println("Tarih,Tutar,Durum,Açıklama");
                for (int i = 0; i < zahlungsModel.getRowCount(); i++) {
                    String tarih = escapeCSV(zahlungsModel.getValueAt(i, 0).toString());
                    String tutar = String.format(Locale.US, "%.2f", Double.parseDouble(zahlungsModel.getValueAt(i, 1).toString()));
                    String durum = escapeCSV(zahlungsModel.getValueAt(i, 2).toString());
                    String aciklama = escapeCSV(zahlungsModel.getValueAt(i, 3).toString());
                    writer.printf("%s,%s,%s,%s%n", tarih, tutar, durum, aciklama);
                }
                JOptionPane.showMessageDialog(this, "CSV başarıyla kaydedildi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // çift tırnakları kaçır
            return "\"" + value + "\"";
        }
        return value;
    }


    private String chooseRootFolderIfNotSelected() {
        if (globalerSpeicherPfad != null) return globalerSpeicherPfad;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Lütfen temel dosya klasörünü seçin");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            globalerSpeicherPfad = chooser.getSelectedFile().getAbsolutePath();
            return globalerSpeicherPfad;
        } else {
            JOptionPane.showMessageDialog(this, "Dosya klasörü seçilmedi.");
            return System.getProperty("user.home"); // Notlösung
        }
    }


    private void islemOeffnen() {
        int selectedRow = verfahrenTabelle.getSelectedRow();
        if (selectedRow >= 0) {
            String datum = (String) verfahrenModel.getValueAt(selectedRow, 0);
            String beschreibung = (String) verfahrenModel.getValueAt(selectedRow, 1);

            String ordnerName = kiraci.getVorname() + "_" + kiraci.getNachname();
            String basisPfad = chooseRootFolderIfNotSelected(); // siehe Schritt 3
            File verfahrensOrdner = new File(basisPfad, ordnerName + "/" + beschreibung);

            if (!verfahrensOrdner.exists()) {
                verfahrensOrdner.mkdirs();
            }

            new IslemFenster(verfahrensOrdner);
        }
    }


    private void zahlungAlsBezahltMarkieren() {
        int selectedRow = zahlungstabelle.getSelectedRow();
        if (selectedRow >= 0) {
            List<Zahlung> zahlungen = ZahlungDAO.getZahlungenFuerKiraci(kiraci.getId());
            Zahlung z = zahlungen.get(selectedRow);

            if ("Bekliyor".equals(z.getTyp())) {
                // Typ ändern auf Ödendi
                Zahlung aktualisiert = new Zahlung(z.getKiraciId(), z.getDatum(), z.getBetrag(), "Ödendi", z.getVermerk());
                aktualisiert.setId(z.getId());
                ZahlungDAO.zahlungAlsBezahltMarkieren(aktualisiert);
            }

            zahlungenLadenUndGuthabenBerechnen();
        }
    }

    private void zahlungLoeschen() {
        int selectedRow = zahlungstabelle.getSelectedRow();
        if (selectedRow >= 0) {
            int bestaetigung = JOptionPane.showConfirmDialog(
                    this,
                    "Bu ödemeyi silmek istiyor musunuz?",
                    "Silme Onayı",
                    JOptionPane.YES_NO_OPTION
            );

            if (bestaetigung == JOptionPane.YES_OPTION) {
                List<Zahlung> zahlungen = ZahlungDAO.getZahlungenFuerKiraci(kiraci.getId());
                Zahlung z = zahlungen.get(selectedRow);
                ZahlungDAO.zahlungLoeschen(z.getId());
                zahlungenLadenUndGuthabenBerechnen();
            }
        }
    }


    private void verfahrenHinzufuegen(int kiraciId) {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner datumSpinner = new JSpinner(dateModel);
        datumSpinner.setEditor(new JSpinner.DateEditor(datumSpinner, "yyyy-MM-dd"));

        JTextField beschreibungFeld = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Açık", "Kapalı"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Tarih:"));
        panel.add(datumSpinner);
        panel.add(new JLabel("İşlem:"));
        panel.add(beschreibungFeld);
        panel.add(new JLabel("Durum:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Yeni işlem ekle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                java.util.Date utilDate = (java.util.Date) datumSpinner.getValue();
                java.sql.Date datum = new java.sql.Date(utilDate.getTime());
                String beschreibung = beschreibungFeld.getText();
                String status = (String) statusCombo.getSelectedItem();

                db.VerfahrenDAO.verfahrenHinzufuegen(kiraciId, datum, beschreibung, status);
                JOptionPane.showMessageDialog(this, "İşlem eklendi!");
                ladeVerfahrenTabelle(kiraciId);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Hatalı giriş!");
            }
        }
    }

    private void kiraDepozitoEingeben(int kiraciId) {
        double aktuelleMiete = ImmobilieDAO.getMiete(kiraciId);
        double aktuelleKaution = ImmobilieDAO.getKaution(kiraciId);
        java.sql.Date aktuellerZahlungstag = ImmobilieDAO.getZahlungstag(kiraciId);

        boolean bereitsVorhanden = aktuelleMiete > 0 || aktuelleKaution > 0;

        JTextField mieteFeld = new JTextField(bereitsVorhanden ? String.valueOf(aktuelleMiete) : "");
        JTextField kautionFeld = new JTextField(bereitsVorhanden ? String.valueOf(aktuelleKaution) : "");

        // Date-Picker vorbereiten
        SpinnerDateModel dateModel = new SpinnerDateModel();
        if (aktuellerZahlungstag != null) {
            dateModel.setValue(aktuellerZahlungstag);
        }
        JSpinner zahlungstagSpinner = new JSpinner(dateModel);
        zahlungstagSpinner.setEditor(new JSpinner.DateEditor(zahlungstagSpinner, "yyyy-MM-dd"));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel(bereitsVorhanden ? "Kira (güncelle):" : "Kira (₺):"));
        panel.add(mieteFeld);
        panel.add(new JLabel(bereitsVorhanden ? "Depozito (güncelle):" : "Depozito (₺):"));
        panel.add(kautionFeld);
        panel.add(new JLabel("Ödeme Başlangıç Tarihi:"));
        panel.add(zahlungstagSpinner);

        int result = JOptionPane.showConfirmDialog(this,
                panel,
                bereitsVorhanden ? "Mevcut verileri güncelle" : "Kira ve Depozito gir",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double miete = Double.parseDouble(mieteFeld.getText());
                double kaution = Double.parseDouble(kautionFeld.getText());
                java.util.Date utilDate = (java.util.Date) zahlungstagSpinner.getValue();
                java.sql.Date zahlungstag = new java.sql.Date(utilDate.getTime());

                ImmobilieDAO.mieteUndKautionEintragen(kiraciId, miete, kaution, zahlungstag);

                // Automatische Einträge sofort nachtragen
                ImmobilieDAO.pruefeUndTrageMieteEin(kiraciId);

                // Oberfläche aktualisieren
                zahlungenLadenUndGuthabenBerechnen();
                aktualisiereImmobilienDaten();

                JOptionPane.showMessageDialog(this, "Veriler kaydedildi ve otomatik ödemeler eklendi!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Geçersiz giriş!");
            }
        }
    }

    private void ladeVerfahrenTabelle(int kiraciId) {
        verfahrenModel.setRowCount(0);

        List<String[]> eintraege = db.VerfahrenDAO.getVerfahrenFuerKiraci(kiraciId); // Liefert List<String[]> z. B. {"2025-05-17", "Notiz", "Açık"}

        for (String[] eintrag : eintraege) {
            verfahrenModel.addRow(eintrag);
        }
    }

    private void aktualisiereImmobilienDaten() {
        double miete = ImmobilieDAO.getMiete(kiraci.getId());
        if (miete > 0) {
            mieteLabel.setText("Kira: " + miete + " ₺");
        } else {
            mieteLabel.setText("Kira: Girilmemiş");
        }

        double kaution = ImmobilieDAO.getKaution(kiraci.getId());
        if (kaution > 0) {
            kautionLabel.setText("Depozito: " + kaution + " ₺");
        } else {
            kautionLabel.setText("Depozito: Girilmemiş");
        }
    }


    private void zahlungenLadenUndGuthabenBerechnen() {
        zahlungsModel.setRowCount(0);
        List<Zahlung> zahlungen = ZahlungDAO.getZahlungenFuerKiraci(kiraci.getId());
        double guthaben = 0;

        for (Zahlung z : zahlungen) {
            zahlungsModel.addRow(new Object[]{
                    z.getDatum(),
                    z.getBetrag(),
                    z.getTyp(),
                    z.getVermerk()
            });

            if ("Bekliyor".equals(z.getTyp())) {
                guthaben += z.getBetrag();
            }
        }

        guthabenLabel.setText(String.format("%.2f ₺", guthaben));
        guthabenLabel.setForeground(guthaben < 0 ? Color.RED : new Color(0, 128, 0));
    }

    private void zahlungHinzufuegen() {
        // Date-Picker mit aktuellem Datum
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner datumSpinner = new JSpinner(dateModel);
        datumSpinner.setEditor(new JSpinner.DateEditor(datumSpinner, "yyyy-MM-dd"));

        JTextField betragFeld = new JTextField();
        JTextField vermerkFeld = new JTextField();
        JComboBox<String> typCombo = new JComboBox<>(new String[]{"Bekliyor", "Ödendi"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Tarih:"));
        panel.add(datumSpinner);
        panel.add(new JLabel("Tutar:"));
        panel.add(betragFeld);
        panel.add(new JLabel("Durum:"));
        panel.add(typCombo);
        panel.add(new JLabel("Açıklama:"));
        panel.add(vermerkFeld);

        int result = JOptionPane.showConfirmDialog(this, panel, "Yeni Ödeme", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                java.util.Date utilDate = (java.util.Date) datumSpinner.getValue();
                java.sql.Date datum = new java.sql.Date(utilDate.getTime());

                double betrag = Double.parseDouble(betragFeld.getText());
                String typ = (String) typCombo.getSelectedItem();
                String vermerk = vermerkFeld.getText();

                Zahlung z = new Zahlung(kiraci.getId(), datum, betrag, typ, vermerk);
                ZahlungDAO.zahlungHinzufuegen(z);
                zahlungenLadenUndGuthabenBerechnen();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Geçersiz giriş! Lütfen tüm alanları doğru doldurun.");
            }
        }
    }
}