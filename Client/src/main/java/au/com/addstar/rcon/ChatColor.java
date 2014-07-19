package au.com.addstar.rcon;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

public enum ChatColor
{
	BLACK('0', Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString(), "black"),
	DARK_BLUE('1', Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString(), "darkblue"),
	DARK_GREEN('2', Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString(), "darkgreen"),
	DARK_AQUA('3', Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString(), "darkaqua", "darkcyan"),
	DARK_RED('4', Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString(), "darkred"),
	DARK_PURPLE('5', Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString(), "darkpurple", "darkmagenta"),
	GOLD('6', Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString(), "gold", "orange", "darkyellow"),
	GRAY('7', Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString(), "gray", "grey", "silver"),
	DARK_GRAY('8', Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString(), "darkgray", "darkgrey"),
	BLUE('9', Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString(), "blue"),
	GREEN('a', Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString(), "green", "lime"),
	AQUA('b', Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString(), "aqua", "cyan"),
	RED('c', Ansi.ansi().fg(Ansi.Color.RED).bold().toString(), "red"),
	LIGHT_PURPLE('d', Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString(), "purple", "magenta"),
	YELLOW('e', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString(), "yellow"),
	WHITE('f', Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString(), "white"),
	
	MAGIC('k', Ansi.ansi().a(Attribute.BLINK_SLOW).toString(), "magic"),
	BOLD('l', Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString(), "bold"),
	STRIKETHROUGH('m', Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString(), "strikethrough", "strike"),
	UNDERLINE('n', Ansi.ansi().a(Attribute.UNDERLINE).toString(), "underline"),
	ITALIC('o', Ansi.ansi().a(Attribute.ITALIC).toString(), "italic"),
	RESET('r', Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString(), "reset");
	
	public static final char COLORCHAR = '\u00A7';
	private static HashMap<Character, ChatColor> mMap = new HashMap<Character, ChatColor>();
	private static HashMap<String, ChatColor> mNameMap = new HashMap<String, ChatColor>();
	
	private final char mChar;
	private final String mAnsi;
	private final String[] mNames; 
	
	private ChatColor(char c, String ansi, String... names)
	{
		mChar = c;
		mAnsi = ansi;
		mNames = names;
	}
	
	public char getChar()
	{
		return mChar;
	}
	
	public String getAnsi()
	{
		return mAnsi;
	}
	
	@Override
	public String toString()
	{
		return mAnsi;
	}
	
	public static ChatColor getByChar(char c)
	{
		return mMap.get(c);
	}
	
	public static ChatColor getByName(String name)
	{
		return mNameMap.get(name.toLowerCase());
	}
	
	private static Pattern mBGErasorPattern = Pattern.compile("\u001B\\[[0-9]{1,2};([0-9]{1,2};[0-9]{1,2})m");
	
	public static String translateColors(String string)
	{
		Matcher m = mBGErasorPattern.matcher(string);
		string = m.replaceAll("\u001B[$1m");
		
		if(string.endsWith("\u001B[m"))
			string = string.substring(0, string.length() - 3);
		
		for (ChatColor color : values()) 
            string = string.replaceAll(String.valueOf(ChatColor.COLORCHAR) + color.getChar(), color.getAnsi());
		return string;
	}
	
	static
	{
		for(ChatColor color : values())
		{
			mMap.put(color.getChar(), color);
			for(String name : color.mNames)
				mNameMap.put(name, color);
		}
	}
}
