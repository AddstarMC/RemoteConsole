package au.com.addstar.rcon;
import java.util.List;

import jline.console.completer.Completer;

public class TabCompleter implements Completer
{
	private ConsoleScreen mScreen;
	public TabCompleter(ConsoleScreen screen)
	{
		mScreen = screen;
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public int complete( String buffer, int cursor, List candidates )
	{
		List<String> results = ClientMain.handleTabComplete(mScreen, buffer);
		
		if(results == null)
			return cursor;
		
		candidates.addAll(results);
		
		 int lastSpace = buffer.lastIndexOf(' ');
         if (lastSpace == -1)
             return cursor - buffer.length();
         else
             return cursor - (buffer.length() - lastSpace - 1);
	}
}
