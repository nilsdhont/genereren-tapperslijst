package be.brigandze;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static be.brigandze.Shift.vertaalDayOfWeek;

public class Tapper implements Comparable<Tapper>{

    private String naam;
    private Ploeg ploeg;

    private boolean isTrainer;
    private int aantalMatchen;
    private int aantalTrainingen;

    public Tapper(String naam, Ploeg ploeg, boolean isTrainer, int aantalMatchen, int aantalTrainingen) {
        this.naam = naam;
        this.ploeg = ploeg;
        this.isTrainer = isTrainer;
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
        return "be.brigandze.Tapper{" +
            "naam='" + naam + '\'' +
            ", ploeg=" + ploeg +
            ", aantalMatchen=" + aantalMatchen +
            ", aantalTrainingen=" + aantalTrainingen +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tapper tapper = (Tapper) o;
        return naam.equals(tapper.naam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam);
    }

    public String[] createCVSLine(List<Shift> listForTapper) {
        List<String> list = new ArrayList<>();
        list.add(naam);
        list.add(String.valueOf(aantalMatchen));
        list.add(String.valueOf(aantalTrainingen));
        listForTapper.forEach(shift -> {
            StringBuilder s = new StringBuilder();
            s.append(vertaalDayOfWeek(shift.startDateTime().getDayOfWeek()));
            s.append(" ");
            s.append(shift.startDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            s.append(" ");
            s.append(shift.beschrijving());
            list.add(s.toString());
        });
        return list.toArray(new String[0]);
    }

    @Override
    public int compareTo(Tapper t) {
        return this.naam.compareTo(t.naam);
    }
}
