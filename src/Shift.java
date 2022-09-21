import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record Shift(LocalDateTime localDateTime, DayOfWeek dayOfWeek, List<Tapper> tappers) {

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(localDateTime.getDayOfWeek());
        s.append(" ");
        s.append(localDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        s.append(" ");
        s.append("Tappers: ");
        tappers.forEach(tapper -> {
            s.append(tapper.getNaam());
            s.append(" || ");
        });
        return s.toString();
    }
}

