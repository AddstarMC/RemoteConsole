package au.com.addstar.rcon.config;

import java.util.HashMap;

import net.cubespace.Yamler.Config.Config;

public class UserStorageConfig extends Config
{
	public HashMap<String, UserConfig> users = new HashMap<String, UserConfig>();
}
