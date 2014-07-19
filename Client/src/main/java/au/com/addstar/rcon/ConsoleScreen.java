package au.com.addstar.rcon;

import java.io.IOException;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Attribute;

public class ConsoleScreen extends Thread
{
	private ConsoleReader mConsole;
	
	public ConsoleScreen()
	{
		super("ConsoleReader");
		setDaemon(true);
		
		try
		{
			mConsole = new ConsoleReader();
			mConsole.setBellEnabled(false);
			mConsole.addCompleter(new TabCompleter(this));
			mConsole.setHandleUserInterrupt(true);
			mConsole.getTerminal().init();
		}
		catch(IOException e)
		{
			throw new UnsupportedOperationException("No console available");
		}
		catch ( Exception e )
		{
			throw new UnsupportedOperationException("No console available");
		}
	}
	
	public String readUsername()
	{
		try
		{
			String result = mConsole.readLine("username>");
			mConsole.setPrompt(null);
			AnsiConsole.out.print(ConsoleReader.RESET_LINE);
			
			return result;
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	public String readPassword()
	{
		try
		{
			String result = mConsole.readLine("password>", '\0');
			mConsole.setPrompt(null);
			AnsiConsole.out.print(ConsoleReader.RESET_LINE);
			
			return result;
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	public void printString(String string)
	{
		try
		{
            AnsiConsole.out.println(ConsoleReader.RESET_LINE + string + Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));
			
			mConsole.redrawLine();
			mConsole.flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void printErrString(String string)
	{
		try
		{
            AnsiConsole.err.println(ConsoleReader.RESET_LINE + string + Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));
			
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
				String command = mConsole.readLine((ClientMain.showPrompt ? ">" : ""));
				
				if(command != null) // Handle commands
					ClientMain.handleCommand(this, command);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(UserInterruptException e)
		{
			System.exit(1);
		}
	}
	
	public void clear()
	{
		try
		{
			mConsole.clearScreen();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
