package com.osiris.jsqlgen.ui.timer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeString {

    public static String toSimpleString(Duration duration) {
        return duration.toString().replace("PT", "").replace("H", "h ")
            .replace("M", "m ").replace("S", "s");
    }

    public static String toSimpleString(LocalDateTime time) {
        return DateTimeFormatter.ofPattern("d MMM uuuu").format(time);
    }
}
