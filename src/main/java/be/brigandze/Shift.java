package be.brigandze;

import org.apache.commons.lang3.ArrayUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public record Shift(LocalDateTime startDateTime, LocalTime endTime, DayOfWeek dayOfWeek, List<Tapper> tappers, String beschrijving) {

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(startDateTime.getDayOfWeek());
        s.append(" ");
        s.append(startDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        s.append(" ");
        s.append("Tappers: ");
        tappers.forEach(tapper -> {
            s.append(tapper.getNaam());
            s.append(" || ");
        });
        return s.toString();
    }

    public String[] toArray() {
        List<String> tempList = new ArrayList<>();
        tempList.add(startDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        tempList.add(beschrijving);
        tempList.add(vertaalDayOfWeek(startDateTime.getDayOfWeek()));

        tempList.add(startDateTime.format(DateTimeFormatter.ofPattern("HH.mm")) + " - " + endTime.format(DateTimeFormatter.ofPattern("HH.mm")));
        tappers.forEach(tapper -> tempList.add(tapper.getNaam()));
        return tempList.toArray(new String[0]);
    }

    public static String vertaalDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
        case MONDAY -> {
            return "Maandag";
        }
        case TUESDAY -> {
            return "Dinsdag";
        }
        case THURSDAY -> {
            return "Donderdag";
        }
        case SATURDAY -> {
            return "Zaterdag";
        }
        case SUNDAY -> {
            return "Zondag";
        }
        default -> {
            return "";
        }
        }
    }
}

