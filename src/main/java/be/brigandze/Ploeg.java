package be.brigandze;

public enum Ploeg {
    MANNEN(false, true, true),
    VROUWEN(false, true, true),
    TOUCH(false, false, true),
    U6(true, false, false),
    U8(true, false, false),
    U10(true, false, false),
    U12(true, false, false),
    U14(true, false, false),
    U16(true, false, false),
    U18(true, false, false),
    U15_U18_POWER_GIRLS(true, false, false);

    Ploeg(boolean jeugdPloeg, boolean taptSeniorenTraining, boolean taptSeniorenMatchen) {
        this.jeugdPloeg = jeugdPloeg;
        this.taptSeniorenTraining = taptSeniorenTraining;
        this.taptSeniorenMatchen = taptSeniorenMatchen;
    }

    private boolean jeugdPloeg;
    private boolean taptSeniorenTraining;
    private boolean taptSeniorenMatchen;

    public boolean isJeugdPloeg() {
        return jeugdPloeg;
    }

    public boolean isTaptSeniorenTraining() {
        return taptSeniorenTraining;
    }

    public boolean isTaptSeniorenMatchen() {
        return taptSeniorenMatchen;
    }
}
