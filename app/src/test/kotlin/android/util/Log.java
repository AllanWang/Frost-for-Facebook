package android.util;

import java.util.Locale;

/**
 * Created by Allan Wang on 31/12/17.
 */
public class Log {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    public static int e(String tag, String msg, Throwable t) {
        System.err.println("ERROR: " + tag + ": " + msg + "\n\tThrowable: " + t.getMessage());
        return 0;
    }

    public static int println(int priority, String tag, String message) {
        switch (priority) {
            case VERBOSE:
                p("V", tag, message);
                break;
            case INFO:
                p("I", tag, message);
                break;
            case DEBUG:
                p("D", tag, message);
                break;
            case ERROR:
                p("E", tag, message);
                break;
            default:
                p("L " + priority, tag, message);
                break;
        }
        return 0;
    }

    private static void p(String flag, String tag, String msg) {
        System.out.println(String.format(Locale.CANADA, "%s: %s: %s", flag, tag, msg));
    }

}