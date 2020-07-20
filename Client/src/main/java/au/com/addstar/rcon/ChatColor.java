package au.com.addstar.rcon;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChatColor {
    BLACK('0', Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString(), false, "black"),
    DARK_BLUE('1', Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString(), false, "darkblue"),
    DARK_GREEN('2', Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString(), false, "darkgreen"),
    DARK_AQUA('3', Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString(), false, "darkaqua", "darkcyan"),
    DARK_RED('4', Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString(), false, "darkred"),
    DARK_PURPLE('5', Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString(), false, "darkpurple", "darkmagenta"),
    GOLD('6', Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString(), false, "gold", "orange", "darkyellow"),
    GRAY('7', Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString(), false, "gray", "grey", "silver"),
    DARK_GRAY('8', Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString(), false, "darkgray", "darkgrey"),
    BLUE('9', Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString(), false, "blue"),
    GREEN('a', Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString(), false, "green", "lime"),
    AQUA('b', Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString(), false, "aqua", "cyan"),
    RED('c', Ansi.ansi().fg(Ansi.Color.RED).bold().toString(), false, "red"),
    LIGHT_PURPLE('d', Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString(), false, "purple", "magenta"),
    YELLOW('e', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString(), false, "yellow"),
    WHITE('f', Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString(), false, "white"),

    MAGIC('k', Ansi.ansi().a(Attribute.BLINK_SLOW).toString(), true, "magic"),
    BOLD('l', Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString(), true, "bold"),
    STRIKETHROUGH('m', Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString(), true, "strikethrough", "strike"),
    UNDERLINE('n', Ansi.ansi().a(Attribute.UNDERLINE).toString(), true, "underline"),
    ITALIC('o', Ansi.ansi().a(Attribute.ITALIC).toString(), true, "italic"),
    RESET('r', Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString(), false, "reset");

    public static final char COLORCHAR = '\u00A7';
    public static final char ESC_CHAR = '\u001B';
    private static final String SOFT_RESET = Ansi.ansi().a(Attribute.RESET).toString();

    private static final HashMap<Character, ChatColor> mMap = new HashMap<>();
    private static final HashMap<String, ChatColor> mNameMap = new HashMap<>();

    private final char mChar;
    private final String mAnsi;
    private final String[] mNames;
    private final boolean mIsFormat;

    ChatColor(char c, String ansi, boolean isFormat, String... names) {
        mChar = c;
        mAnsi = ansi;
        mNames = names;
        mIsFormat = isFormat;
    }

    public char getChar() {
        return mChar;
    }

    public String getAnsi() {
        return mAnsi;
    }

    public boolean isFormat() {
        return mIsFormat;
    }

    @Override
    public String toString() {
        return mAnsi;
    }

    public static ChatColor getByChar(char c) {
        return mMap.get(c);
    }

    public static ChatColor getByName(String name) {
        return mNameMap.get(name.toLowerCase());
    }

    private static final Pattern mBGErasurePattern = Pattern.compile(ESC_CHAR+"\\[4[0-8];([0-9]{1,2};[0-9]{1,2})m");
    private static final Pattern mTranslatePattern = Pattern.compile(COLORCHAR+"([a-frl-o0-9k])",
          Pattern.CASE_INSENSITIVE);
    private static final Pattern mRGGTranslatePattern = Pattern.compile(COLORCHAR+"x("+COLORCHAR+"[A-F0-9]){6}",
          Pattern.CASE_INSENSITIVE);
    private static final Pattern mColorErasurePattern = Pattern.compile("(?:"+ESC_CHAR+"\\[.*?m|"+COLORCHAR+"[a-fA-F0-9rl-o])");

    private static final String RGBFORMAT = ESC_CHAR+"[38;2;%d;%d;%dm";

    public static String translateColors(String string) {
        Matcher m = mBGErasurePattern.matcher(string);
        string = m.replaceAll(ESC_CHAR+"[$1m");

		if (string.endsWith(ESC_CHAR+"[m")) {
			string = string.substring(0, string.length() - 3);
		}
        string = replaceRGBColors(string);
        m = mTranslatePattern.matcher(string);
        StringBuffer buffer = new StringBuffer();
        boolean format = false;
        while (m.find()) {
            ChatColor col = getByChar(m.group(1).toLowerCase().charAt(0));
            String replacement = "";
			if (col.isFormat()) {
				format = true;
			} else {
				if (format && col != RESET) {
					replacement = SOFT_RESET;
				}
				format = false;
			}
            replacement += col.getAnsi();

            m.appendReplacement(buffer, replacement);
        }

        m.appendTail(buffer);
        return buffer.toString();
    }

    private static String replaceRGBColors(String string) {
		Matcher m = mRGGTranslatePattern.matcher(string);
		StringBuffer buffer1 = new StringBuffer();
		while (m.find()) {
			String s = m.group().replace("§", "").replace('x','#');
			Color color = Color.decode(s);
			int red = color.getRed();
			int blue = color.getBlue();
			int green = color.getGreen();
			String replacement = String.format(RGBFORMAT, red, green, blue);
			m.appendReplacement(buffer1, replacement);
		}
		m.appendTail(buffer1);
		return buffer1.toString();
	}

    public static String stripColors(String string) {
        Matcher m = mColorErasurePattern.matcher(string);
        return m.replaceAll("");
    }

    static {
        for (ChatColor color : values()) {
            mMap.put(color.getChar(), color);
			for (String name : color.mNames) {
				mNameMap.put(name, color);
			}
        }
    }
}
