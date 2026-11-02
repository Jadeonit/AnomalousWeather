package ru.anomaly.weather.anomaly;

/** Активная аномалия: тип, настройки и прошедшее время. */
public final class ActiveAnomaly {

    private final AnomalyType type;
    private final WeatherSettings settings;
    private final int durationSeconds;
    private int elapsedSeconds;

    public ActiveAnomaly(AnomalyType type, WeatherSettings settings, int durationSeconds) {
        this.type = type;
        this.settings = settings;
        this.durationSeconds = Math.max(5, durationSeconds);
    }

    /** Вызывается раз в секунду. */
    public void tick() {
        elapsedSeconds++;
    }

    public AnomalyType type() {
        return type;
    }

    public WeatherSettings settings() {
        return settings;
    }

    public int duration() {
        return durationSeconds;
    }

    public int elapsed() {
        return elapsedSeconds;
    }

    public int remaining() {
        return Math.max(0, durationSeconds - elapsedSeconds);
    }

    public boolean finished() {
        return elapsedSeconds >= durationSeconds;
    }

    /** Прошло ли целое число секунд (для интервалов эффектов). */
    public boolean every(int seconds) {
        return seconds > 0 && elapsedSeconds > 0 && elapsedSeconds % seconds == 0;
    }
}
