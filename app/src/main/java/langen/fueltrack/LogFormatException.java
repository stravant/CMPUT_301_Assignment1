package langen.fueltrack;

/**
 * Exception generated when setting a field of a LogEntry to an invalid value
 */
public class LogFormatException extends Exception {
    public LogFormatException(String str) {
        this.message = str;
    }

    public String getMessage() {
        return message;
    }

    private String message;
}
