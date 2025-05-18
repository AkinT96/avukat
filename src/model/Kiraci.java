package model;

public class Kiraci {
    private int id;
    private String vorname;
    private String nachname;
    private String adresse;
    private String vermieter;
    private double guthaben;

    public Kiraci(int id, String vorname, String nachname, String adresse, String vermieter, double guthaben) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.adresse = adresse;
        this.vermieter = vermieter;
        this.guthaben = guthaben;
    }

    public Kiraci(String vorname, String nachname, String adresse, String vermieter) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.adresse = adresse;
        this.vermieter = vermieter;
        this.guthaben = 0.0;
    }

    // Getter und Setter
    public int getId() { return id; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public String getAdresse() { return adresse; }
    public String getVermieter() { return vermieter; }
    public double getGuthaben() { return guthaben; }

    public void setId(int id) { this.id = id; }
    public void setVorname(String vorname) { this.vorname = vorname; }
    public void setNachname(String nachname) { this.nachname = nachname; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setVermieter(String vermieter) { this.vermieter = vermieter; }
    public void setGuthaben(double guthaben) { this.guthaben = guthaben; }
}
