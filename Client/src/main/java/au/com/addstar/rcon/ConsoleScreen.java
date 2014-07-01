package au.com.addstar.rcon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jline.console.ConsoleReader;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Attribute;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;

public class ConsoleScreen extends Thread
{
	private static final Map<Character, String> mColors = new HashMap<Character, String>();
	private static String mColorChar = "\u00A7";
	
	static
	{
		mColors.put('0', Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString());
        mColors.put('1', Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString());
        mColors.put('2', Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString());
        mColors.put('3', Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString());
        mColors.put('4', Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString());
        mColors.put('5', Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString());
        mColors.put('6', Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString());
        mColors.put('7', Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
        mColors.put('8', Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
        mColors.put('9', Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
        mColors.put('a', Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
        mColors.put('b', Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
        mColors.put('c', Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
        mColors.put('d', Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString());
        mColors.put('e', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        mColors.put('f', Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
        mColors.put('k', Ansi.ansi().a(Attribute.BLINK_SLOW).toString());
        mColors.put('l', Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString());
        mColors.put('m', Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString());
        mColors.put('n', Ansi.ansi().a(Attribute.UNDERLINE).toString());
        mColors.put('o', Ansi.ansi().a(Attribute.ITALIC).toString());
        mColors.put('r', Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
	}
	
	private ConsoleReader mConsole;
	
	public ConsoleScreen()
	{
		super("ConsoleReader");
		setDaemon(true);
		
		try
		{
			mConsole = new ConsoleReader();
			mConsole.setBellEnabled(false);
			mConsole.addCompleter(new TabCompleter());
		}
		catch(IOException e)
		{
			throw new UnsupportedOperationException("No console available");
		}
	}
	
	public void printString(String string)
	{
		try
		{
			string = string.replaceAll("Â", "");
            for (Character color : mColors.keySet()) 
                string = string.replaceAll(mColorChar + color, mColors.get(color));
			
            AnsiConsole.out.println(ConsoleReader.RESET_LINE + string + Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));
			
			mConsole.redrawLine();
			mConsole.flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				String command = mConsole.readLine(">");
				
				if(command != null) // Handle commands
				{
					if(command.equals("exit"))
					{
						ClientMain.callEvent(new Event(EventType.Quit));
						break;
					}
					
					if(ClientMain.getConnectionManager().getActive() != null)
						ClientMain.getConnectionManager().getActive().sendPacket(new PacketInCommand(command));
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
