package be.brigandze;

public enum Ploeg {
    MANNEN(false, true, true, true),
    VROUWEN(false, true, true, true),
    TOUCH(false, false, true, false),
    TORNOOI(true, false, false, false),
    U6(true, false, false, false),
    U8(true, false, false, false),
    U10(true, false, false, false),
    U12(true, false, false, false),
    U14(true, false, false, false),
    U16(true, false, false, false),
    U18(true, false, false, false),
    U15_U18_POWER_GIRLS(true, false, false, false);

    Ploeg(boolean jeugdPloeg, boolean taptSeniorenTraining, boolean taptSeniorenMatchen, boolean taptJeugd) {
        this.jeugdPloeg = jeugdPloeg;
        this.taptSeniorenTraining = taptSeniorenTraining;
        this.taptSeniorenMatchen = taptSeniorenMatchen;
        this.taptJeugd = taptJeugd;
    }

    private boolean jeugdPloeg;
    private boolean taptSeniorenTraining;
    private boolean taptSeniorenMatchen;
    private boolean taptJeugd;

    public boolean isJeugdPloeg() {
        return jeugdPloeg;
    }

    public boolean isTaptSeniorenTraining() {
        return taptSeniorenTraining;
    }

    public boolean isTaptSeniorenMatchen() {
        return taptSeniorenMatchen;
    }

    public boolean isTaptJeugd() {
        return taptJeugd;
    }
}
