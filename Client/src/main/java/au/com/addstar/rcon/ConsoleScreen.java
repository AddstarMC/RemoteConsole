package au.com.addstar.rcon;

import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Erase;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;

public class ConsoleScreen extends Thread
{
	protected ConsoleReader mConsole;
	private String mPromptText;

	public void setDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}

	private boolean mDebug = false;
	
	public ConsoleScreen()
	{
		super("ConsoleReader");
		setDaemon(true);
		
		mPromptText = "";
		try
		{
			mConsole = new ConsoleReader();
			mConsole.setBellEnabled(false);
			mConsole.setExpandEvents(false);
			mConsole.addCompleter(new TabCompleter(this));
			mConsole.setHandleUserInterrupt(true);
			mConsole.getTerminal().init();
		} catch ( Exception e )
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
	
	protected int getCursorLines()
	{
		int promptSize = (mConsole.getPrompt() != null ? mConsole.getPrompt().length() : 0);
		int size = mConsole.getCursorBuffer().length() + promptSize + 1;
		
		return (int)Math.ceil(size / (double)mConsole.getTerminal().getWidth());
	}
	
	public void printString(String string)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			int lines = getCursorLines();
			while (lines > 1)
			{
				buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
				buffer.append(ConsoleReader.RESET_LINE);
				buffer.append(Ansi.ansi().cursorUp(1));
				--lines;
			}
			
			buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
			buffer.append(ConsoleReader.RESET_LINE);
			
			buffer.append(string);
			buffer.append(Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));
			
			if (mDebug)
			{
				for (int i = 0; i < buffer.length(); ++i)
				{
					if (!Character.isAlphabetic(buffer.charAt(i)) && !Character.isWhitespace(buffer.charAt(i)) && !Character.isDigit(buffer.charAt(i)))
					{
						String insert = String.format("\\%x", (int)buffer.charAt(i));
						buffer.replace(i, i, insert);
						i += insert.length();
					}
				}
			}
			
			AnsiConsole.out.println(buffer.toString());
			
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
			StringBuffer buffer = new StringBuffer();
			int lines = getCursorLines();
			while (lines > 1)
			{
				buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
				buffer.append(ConsoleReader.RESET_LINE);
				buffer.append(Ansi.ansi().cursorUp(1));
				--lines;
			}
			
			buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
			buffer.append(ConsoleReader.RESET_LINE);
			
			buffer.append(string);
			buffer.append(Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));

			if (mDebug)
			{
				for (int i = 0; i < buffer.length(); ++i)
				{
					if (Character.isISOControl(buffer.charAt(i)))
					{
						String insert = String.format("\\%x", (int)buffer.charAt(i));
						buffer.replace(i, i, insert);
						i += insert.length();
					}
				}
			}
			
			AnsiConsole.err.println(buffer.toString());
			
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
				String command = mConsole.readLine((ClientMain.showPrompt ? mPromptText + ">" : ""));
				
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
	
	public void setPromptText(String text)
	{
		mPromptText = text;
		mConsole.setPrompt(mPromptText + ">");
		try
		{
			StringBuffer buffer = new StringBuffer();
			int lines = getCursorLines();
			while (lines > 1)
			{
				buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
				buffer.append(ConsoleReader.RESET_LINE);
				buffer.append(Ansi.ansi().cursorUp(1));
				--lines;
			}
			
			buffer.append(Ansi.ansi().eraseLine(Erase.ALL));
			buffer.append(ConsoleReader.RESET_LINE);
			
			mConsole.print(buffer.toString());
			mConsole.drawLine();
			mConsole.flush();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
