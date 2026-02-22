package utmn.trifonov;

import utmn.trifonov.cli.CommandHandler;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.cli.commands.Command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final Color OUT_COLOR = Color.YELLOW;
    private static final Color WARN_COLOR = Color.YELLOW;
    private static final Color ERR_COLOR = Color.RED;
    private static final Color LOG_COLOR = Color.BLUE;
    private static final Color USR_COLOR = Color.PURPLE;
    private static final Color DIR_COLOR = Color.BRIGHT_BLACK;
    private static final Color FILE_COLOR = Color.BRIGHT_GREEN;
    private static final Color LINE_COLOR = Color.BRIGHT_CYAN;

    public static void out(String text){
        System.out.printf("%s[%s] %s%s%n" + Color.RESET, OUT_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);
    }

    public static void err(String text){
        System.out.printf("%s[%s ERROR]%s %s%n" + Color.RESET, ERR_COLOR , LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);
    }

    public static void wrn(String text){
        System.out.printf("%s[%s WARN] %s%s%n" + Color.RESET, WARN_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                WARN_COLOR, text);
    }

    public static void outL(String text){
        System.out.printf("%s[%s] %s%s" + Color.RESET, LOG_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);
    }

    public static void errL(String text){
        System.out.printf("%s[%s ERROR]%s %s" + Color.RESET, ERR_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);
    }

    public static void wrnL(String text){
        System.out.printf("%s[%s WARN] %s%s" + Color.RESET, WARN_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                WARN_COLOR, text);
    }

    public static void log(String text){
        System.out.printf("%s[%s LOG] %s%s%n" + Color.RESET, LOG_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                Color.CYAN, text);
    }

    public static void pos(String text){
        CommandHandler ch = Main.getCommandHandler();
        boolean isFileLayer = ch instanceof FileLayerCommandHandler;
        FileLayerCommandHandler flch = isFileLayer ? (FileLayerCommandHandler) ch : null;

        System.out.printf("%s[%s %s#%s%s] %s%s%n" + Color.RESET, OUT_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                        (isFileLayer ? (flch.hasChosenLine() ? LINE_COLOR : FILE_COLOR) : DIR_COLOR),
                ch.getLocation().getFormattedPath() +
                        (isFileLayer && flch.hasChosenLine() ? " (%d)".formatted(flch.getChosenLineIdx()) : ""),
                OUT_COLOR,
                DEFAULT_COLOR, text);
    }

    public static void usr(String text){
        pos("%s@%s %s> %s%s".formatted(USR_COLOR, Main.getSession().getUser().getUsername(), OUT_COLOR, DEFAULT_COLOR, text));
    }

    public enum Color{
        BOLD("\u001B[1m"),
        FAINT("\u001B[2m"),
        ITALIC("\u001B[3m"),
        UNDERLINE("\u001B[4m"),
        BLINK("\u001B[5m"),
        REVERSE("\u001B[7m"),
        HIDDEN("\u001B[8m"),
        STRIKETHROUGH("\u001B[9m"),

        BRIGHT_BLACK("\u001B[90m"),
        BRIGHT_RED("\u001B[91m"),
        BRIGHT_GREEN("\u001B[92m"),
        BRIGHT_YELLOW("\u001B[93m"),
        BRIGHT_BLUE("\u001B[94m"),
        BRIGHT_PURPLE("\u001B[95m"),
        BRIGHT_CYAN("\u001B[96m"),
        BRIGHT_WHITE("\u001B[97m"),

        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),

        BLACK_BACKGROUND("\u001B[40m"),
        RED_BACKGROUND("\u001B[41m"),
        GREEN_BACKGROUND("\u001B[42m"),
        YELLOW_BACKGROUND("\u001B[43m"),
        BLUE_BACKGROUND("\u001B[44m"),
        PURPLE_BACKGROUND("\u001B[45m"),
        CYAN_BACKGROUND("\u001B[46m"),
        WHITE_BACKGROUND("\u001B[47m");

        final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

}
