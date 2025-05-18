package model;


public class Mandant {
    private int id; // Mandanten-ID (Müşteri kimliği)
    private String name; // Name des Mandanten (Müşteri adı)
    private String anschrift; // Anschrift des Mandanten (Adres)
    private String telefon; // Telefonnummer (Telefon numarası)

    public Mandant(int id, String name, String anschrift, String telefon) {
        this.id = id;
        this.name = name;
        this.anschrift = anschrift;
        this.telefon = telefon;
    }

    public Mandant(String name, String anschrift, String telefon) {
        this.name = name;
        this.anschrift = anschrift;
        this.telefon = telefon;
    }

    // Getter und Setter (Getter ve Setter metodları)
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAnschrift() {
        return anschrift;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAnschrift(String anschrift) {
        this.anschrift = anschrift;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    @Override
    public String toString() {
        return name + " - " + anschrift + " - " + telefon; // Darstellung im UI (Arayüzde gösterim)
    }
}
