package au.com.addstar.rcon;
import java.util.List;

import jline.console.completer.Completer;

import au.com.addstar.rcon.network.NetworkManager;

public class TabCompleter implements Completer
{
	private NetworkManager mManager;
	public TabCompleter(NetworkManager manager)
	{
		mManager = manager;
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public int complete( String buffer, int cursor, List candidates )
	{
		return cursor;
		
		// TODO: This
		
//		List<String> results = mCon.doTabComplete(buffer);
//		if(results == null)
//			return cursor;
//		
//		candidates.addAll(results);
//		
//		 int lastSpace = buffer.lastIndexOf(' ');
//         if (lastSpace == -1)
//             return cursor - buffer.length();
//         else
//             return cursor - (buffer.length() - lastSpace - 1);
	}

}
