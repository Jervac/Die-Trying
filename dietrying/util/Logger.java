package app.dietrying.util;

public class Logger {
    public static boolean enabled = true;

    public Logger() {
    }

    public static void info(String information) {
        if (Logger.enabled) {
            System.out.println("INFO: " + information);
        }

    }

    public static void warn(String warning) {
        if (Logger.enabled) {
            System.out.println("WARNING: " + warning);
        }

    }

    public static void error(String error, boolean exit) {
        if (Logger.enabled) {
            System.out.println("ERROR: " + error);
            if (exit) {
                System.exit(-1);
            }
        }

    }

    public static void success(String success) {
        if (Logger.enabled) {
            System.out.println("SUCCESS: " + success);
        }

    }

    public static void enable() {
        Logger.enabled = true;
    }

    public static void disable() {
        Logger.enabled = false;
    }

    public static void textureNotFound(String t) {
        if (Logger.enabled) {
            System.out.println("Couldn't load texture: " + t + ". See if it was loaded and/or spelt correctly as well as if sprite or image was used to load it.");
            System.exit(1);
        }
    }
}
