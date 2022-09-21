public class Tapper {

    private String naam;
    private Ploeg ploeg;
    private int aantalMatchen;
    private int aantalTrainingen;

    public Tapper(String naam, Ploeg ploeg, int aantalMatchen, int aantalTrainingen) {
        this.naam = naam;
        this.ploeg = ploeg;
        this.aantalMatchen = aantalMatchen;
        this.aantalTrainingen = aantalTrainingen;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Ploeg getPloeg() {
        return ploeg;
    }

    public void setPloeg(Ploeg ploeg) {
        this.ploeg = ploeg;
    }

    public int getAantalMatchen() {
        return aantalMatchen;
    }

    public void setAantalMatchen(int aantalMatchen) {
        this.aantalMatchen = aantalMatchen;
    }

    public void addMatch(){
        this.aantalMatchen++;
    }

    public int getAantalTrainingen() {
        return aantalTrainingen;
    }

    public void setAantalTrainingen(int aantalTrainingen) {
        this.aantalTrainingen = aantalTrainingen;
    }

    public void addTraining(){
        aantalTrainingen++;
    }

    @Override
    public String toString() {
        return "Tapper{" +
            "naam='" + naam + '\'' +
            ", ploeg=" + ploeg +
            ", aantalMatchen=" + aantalMatchen +
            ", aantalTrainingen=" + aantalTrainingen +
            '}';
    }
}
