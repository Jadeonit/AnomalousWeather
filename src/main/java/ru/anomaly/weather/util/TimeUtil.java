package ru.anomaly.weather.util;

/** Форматирование времени. */
public final class TimeUtil {

    private TimeUtil() {
    }

    public static String format(int totalSeconds) {
        int seconds = Math.max(0, totalSeconds);
        int minutes = seconds / 60;
        int rest = seconds % 60;
        if (minutes <= 0) {
            return rest + " сек";
        }
        return minutes + " мин " + (rest < 10 ? "0" : "") + rest + " сек";
    }
}
