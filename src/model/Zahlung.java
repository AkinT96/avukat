
package model;

import java.sql.Date;

public class Zahlung {
    private int id;
    private int kiraciId;
    private Date datum;
    private double betrag;
    private String typ;
    private String vermerk;

    public Zahlung(int id, int kiraciId, Date datum, double betrag, String typ, String vermerk) {
        this.id = id;
        this.kiraciId = kiraciId;
        this.datum = datum;
        this.betrag = betrag;
        this.typ = typ;
        this.vermerk = vermerk;
    }

    public Zahlung(int kiraciId, Date datum, double betrag, String typ, String vermerk) {
        this.kiraciId = kiraciId;
        this.datum = datum;
        this.betrag = betrag;
        this.typ = typ;
        this.vermerk = vermerk;
    }

    public int getId() { return id; }
    public int getKiraciId() { return kiraciId; }
    public Date getDatum() { return datum; }
    public double getBetrag() { return betrag; }
    public String getTyp() { return typ; }
    public String getVermerk() { return vermerk; }

    public void setId(int id) { this.id = id; }
    public void setKiraciId(int kiraciId) { this.kiraciId = kiraciId; }
    public void setDatum(Date datum) { this.datum = datum; }
    public void setBetrag(double betrag) { this.betrag = betrag; }
    public void setTyp(String typ) { this.typ = typ; }
    public void setVermerk(String vermerk) { this.vermerk = vermerk; }
}
