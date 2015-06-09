package au.com.addstar.rcon.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Whitelist
{
	private List<String> mWhitelist = Collections.emptyList();
	
	public void load(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try
		{
			mWhitelist = new ArrayList<String>();
			
			String line;
			while((line = reader.readLine()) != null)
			{
				// Ignore comments and empty lines
				if (line.startsWith("#") || line.trim().isEmpty())
					continue;
				
				mWhitelist.add(line.trim().toLowerCase());
			}
		}
		finally
		{
			reader.close();
		}
	}
	
	public boolean isWhitelisted(String fullCommand)
	{
		fullCommand = fullCommand.toLowerCase();
		for (String command : mWhitelist)
		{
			if (fullCommand.startsWith(command))
				return true;
		}
		
		return false;
	}
}
