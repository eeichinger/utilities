package cuke4duke;

class AssertUtils {
    static void notNull(Object arg, String message) {
        if (arg == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
