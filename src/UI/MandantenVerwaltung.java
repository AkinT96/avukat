package UI;

import db.MandantDAO;
import model.Mandant;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MandantenVerwaltung extends JPanel {

    private DefaultListModel<Mandant> listModel = new DefaultListModel<>();
    private JList<Mandant> mandantenJList = new JList<>(listModel);

    public MandantenVerwaltung() {
        setLayout(new BorderLayout());

        JButton hinzufuegenButton = new JButton("Mandant hinzufügen (Müşteri ekle)");
        JButton loeschenButton = new JButton("Mandant löschen (Müşteriyi sil)");

        hinzufuegenButton.addActionListener(e -> mandantHinzufuegen());
        loeschenButton.addActionListener(e -> mandantLoeschen());

        JPanel buttonPanel = new JPanel(); // Button-Panel unten (Buton paneli aşağıda)
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(hinzufuegenButton);
        buttonPanel.add(loeschenButton);

        add(new JScrollPane(mandantenJList), BorderLayout.CENTER); // Mandantenliste in der Mitte (Müşteri listesi ortada)
        add(buttonPanel, BorderLayout.SOUTH); // Buttons unten einfügen (Butonları alta ekle)

        mandantenLaden(); // Mandanten laden (Müşterileri yükle)
    }

    private void mandantenLaden() {
        listModel.clear();
        List<Mandant> mandanten = MandantDAO.getAlleMandanten();
        for (Mandant m : mandanten) {
            listModel.addElement(m); // Jeden Mandanten zur Liste hinzufügen (Her müşteriyi listeye ekle)
        }
    }

    private void mandantHinzufuegen() {
        String name = JOptionPane.showInputDialog(this, "Name eingeben (İsim girin):");
        String anschrift = JOptionPane.showInputDialog(this, "Anschrift eingeben (Adres girin):");
        String telefon = JOptionPane.showInputDialog(this, "Telefonnummer eingeben (Telefon numarası girin):");

        if (name != null && anschrift != null && telefon != null) {
            Mandant m = new Mandant(name, anschrift, telefon);
            MandantDAO.mandantHinzufuegen(m);
            mandantenLaden(); // Liste aktualisieren (Listeyi güncelle)
        }
    }

    private void mandantLoeschen() {
        Mandant ausgewaehlt = mandantenJList.getSelectedValue(); // Auswahl ermitteln (Seçilen müşteri)
        if (ausgewaehlt != null) {
            int bestaetigung = JOptionPane.showConfirmDialog(
                    this,
                    "Möchtest du den Mandanten wirklich löschen? (Müşteriyi silmek istiyor musun?)",
                    "Löschen bestätigen (Silme onayı)",
                    JOptionPane.YES_NO_OPTION
            );

            if (bestaetigung == JOptionPane.YES_OPTION) {
                MandantDAO.mandantLoeschen(ausgewaehlt.getId());
                mandantenLaden(); // Liste aktualisieren (Listeyi yenile)
            }
        } else {
            JOptionPane.showMessageDialog(this, "Bitte wähle einen Mandanten aus (Lütfen bir müşteri seçin)");
        }
    }
}
