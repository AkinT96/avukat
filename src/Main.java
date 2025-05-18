import UI.Hauptfenster;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Hauptfenster(); // Hauptfenster anzeigen (Ana pencereyi göster)
        });
    }
}
