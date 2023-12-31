package be.brigandze;

import java.util.Arrays;

public enum SoortShift {
  TRAINING_JEUGD("Training jeugd"),
  TRAINING_MAANDAG("Training vrouwen"),
  TRAINING_DINSDAG("Training mannen"),
  TRAINING_DONDERDAG("Training mannen/vrouwen"),
  MATCH_SENIOREN_VROUWEN("Match vrouwen"),
  MATCH_SENIOREN_MANNEN("Match mannen"),
  MATCH_JEUGD("Match Jeugd");

  private String beschrijving;

  SoortShift(String beschrijving) {
    this.beschrijving = beschrijving;
  }

  public String getBeschrijving() {
    return beschrijving;
  }

  public static SoortShift fromBeschrijving(String beschrijving) {
    return Arrays.stream(values())
        .filter(soortShift -> soortShift.beschrijving.equalsIgnoreCase(beschrijving))
        .findFirst()
        .orElseThrow();
  }
}
