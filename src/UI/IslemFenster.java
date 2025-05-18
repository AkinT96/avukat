package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

public class IslemFenster extends JFrame {

    private File ordner;
    private JTable tabelle;
    private DefaultTableModel model;
    private Map<String, File> dateinameZuDatei = new HashMap<>();

    public IslemFenster(File ordner) {
        this.ordner = ordner;
        setTitle("İşlem Dosyaları - " + ordner.getName());
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"Tarih", "Dosya", "Notiz"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabelle = new JTable(model);
        JScrollPane scroll = new JScrollPane(tabelle);
        add(scroll, BorderLayout.CENTER);

        tabelle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabelle.rowAtPoint(e.getPoint());
                int col = tabelle.columnAtPoint(e.getPoint());

                String anzeigename = (String) model.getValueAt(row, 1);
                File datei = dateinameZuDatei.get(anzeigename);
                if (datei == null) return;

                if (e.getClickCount() == 2 && col == 1) {
                    try {
                        Desktop.getDesktop().open(datei);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(IslemFenster.this, "Dosya açılamadı:\n" + ex.getMessage());
                    }
                }

                if (e.getClickCount() == 2 && col == 2) {
                    File notizDatei = new File(datei.getAbsolutePath() + ".txt");
                    StringBuilder bisher = new StringBuilder();
                    if (notizDatei.exists()) {
                        try (Scanner sc = new Scanner(notizDatei)) {
                            while (sc.hasNextLine()) {
                                bisher.append(sc.nextLine()).append("\n");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    JTextArea feld = new JTextArea(bisher.toString(), 10, 40);
                    int res = JOptionPane.showConfirmDialog(
                            IslemFenster.this,
                            new JScrollPane(feld),
                            "Notiz bearbeiten: " + anzeigename,
                            JOptionPane.OK_CANCEL_OPTION
                    );

                    if (res == JOptionPane.OK_OPTION) {
                        try (PrintWriter pw = new PrintWriter(notizDatei)) {
                            pw.write(feld.getText());
                            model.setValueAt(feld.getText(), row, 2);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        JButton dateiHinzufuegen = new JButton("Dosya Ekle");
        dateiHinzufuegen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File quelle = chooser.getSelectedFile();
                File ziel = new File(ordner, quelle.getName());
                try {
                    java.nio.file.Files.copy(quelle.toPath(), ziel.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    String anzeigename = JOptionPane.showInputDialog(this, "Dosya için bir ad girin:");
                    if (anzeigename != null && !anzeigename.isBlank()) {
                        File nameDatei = new File(ziel.getAbsolutePath() + ".name.txt");
                        try (FileWriter writer = new FileWriter(nameDatei)) {
                            writer.write(anzeigename);
                        }
                        fuegeDateiHinzu(ziel);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton ordnerZippen = new JButton("ZIP indir");
        ordnerZippen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(ordner.getParentFile().getName() + ".zip"));
            int res = chooser.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File ziel = chooser.getSelectedFile();
                File parentOrdner = ordner.getParentFile();
                try (FileOutputStream fos = new FileOutputStream(ziel);
                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    zippeVerzeichnis(parentOrdner, parentOrdner, zipOut);

                    JOptionPane.showMessageDialog(this, "ZIP oluşturuldu: " + ziel.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "ZIP hatası: " + ex.getMessage());
                }
            }
        });


        JPanel unten = new JPanel();
        unten.add(dateiHinzufuegen);
        unten.add(ordnerZippen);
        add(unten, BorderLayout.SOUTH);

        ladeDateienInTabelle();
        setVisible(true);
    }

    private void zippeVerzeichnis(File basisOrdner, File aktuelleDatei, ZipOutputStream zipOut) throws IOException {
        if (aktuelleDatei.isHidden()) return;

        if (aktuelleDatei.isDirectory()) {
            for (File kind : Objects.requireNonNull(aktuelleDatei.listFiles())) {
                zippeVerzeichnis(basisOrdner, kind, zipOut);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(aktuelleDatei)) {
                String zipEintragName = basisOrdner.toPath().relativize(aktuelleDatei.toPath()).toString();
                zipOut.putNextEntry(new ZipEntry(zipEintragName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zipOut.write(buffer, 0, length);
                }
            }
        }
    }

    private void ladeDateienInTabelle() {
        model.setRowCount(0);
        dateinameZuDatei.clear();

        File[] dateien = ordner.listFiles(file -> {
            String name = file.getName();
            return !name.endsWith(".txt") && !name.endsWith(".name.txt") && !name.endsWith(".zip");
        });

        if (dateien != null) {
            Arrays.sort(dateien, Comparator.comparingLong(File::lastModified));
            for (File f : dateien) {
                fuegeDateiHinzu(f);
            }
        }
    }

    private void fuegeDateiHinzu(File f) {
        String datum = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(f.lastModified()));
        String anzeigename = f.getName();
        File nameDatei = new File(f.getAbsolutePath() + ".name.txt");
        if (nameDatei.exists()) {
            try (Scanner sc = new Scanner(nameDatei)) {
                if (sc.hasNextLine()) {
                    anzeigename = sc.nextLine();
                }
            } catch (Exception ignored) {
            }
        }

        File notizDatei = new File(f.getAbsolutePath() + ".txt");
        StringBuilder notiz = new StringBuilder();
        if (notizDatei.exists()) {
            try (Scanner sc = new Scanner(notizDatei)) {
                while (sc.hasNextLine()) {
                    notiz.append(sc.nextLine()).append("\n");
                }
            } catch (Exception ignored) {
            }
        }

        dateinameZuDatei.put(anzeigename, f);
        model.addRow(new Object[]{datum, anzeigename, notiz.toString().trim()});
    }
}
