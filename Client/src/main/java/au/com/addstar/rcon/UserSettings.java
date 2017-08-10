package au.com.addstar.rcon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UserSettings
{
	public String username;
	public String password;
	
	public UserSettings()
	{
		
	}
	
	public void load(File file) throws IOException
	{

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (line.startsWith("#"))
                    continue;

                String command;
                String args = null;

                if (line.contains(" ")) {
                    int pos = line.indexOf(' ');
                    command = line.substring(0, pos);
                    args = line.substring(pos + 1).trim();
                } else
                    command = line;

                if (command.equals("username")) {
                    if (args == null)
                        throw new IllegalArgumentException(file.getName() + " username missing value");

                    username = args;
                } else if (command.equals("password")) {
                    if (args == null)
                        throw new IllegalArgumentException(file.getName() + " password missing value");

                    password = args;
                } else
                    throw new IllegalArgumentException(file.getName() + " unknown property " + command);
            }
        }
	}
	
	public static UserSettings load()
	{
		File file = new File(new File(System.getProperty("user.home")), ".rconrc");
		
		if(!file.exists())
			return null;
		
		UserSettings settings = new UserSettings();
		try
		{
			settings.load(file);
			return settings;
		}
		catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			return null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
