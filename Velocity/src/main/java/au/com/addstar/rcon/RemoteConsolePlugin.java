package au.com.addstar.rcon;

import au.com.addstar.rcon.commands.RconCommand;
import au.com.addstar.rcon.config.MainConfig;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.MySQLUserStore;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;

/*import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;*/

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
//import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

@Plugin(id = "remoteconsole", version = "${plugin.version}", authors = {"Schmoller"})
public class RemoteConsolePlugin
{
	public static RemoteConsolePlugin instance;

	public static ProxyServer proxyServer;
	public static Logger logger;
	private static RconServer mServer;
	private static MainConfig mConfig;

	private Formatter mFormatter;
	private final Path pluginDir;

	@Inject
	public RemoteConsolePlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.proxyServer = server;
		this.logger = logger;
		this.pluginDir = dataDirectory;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		instance = this;
		mConfig = new MainConfig();

		try
		{
			mConfig.init(new File(pluginDir.toFile(), "config.yml"));
			mConfig.checkValid();
		}
		catch ( InvalidConfigurationException e )
		{
			System.err.println("[RCON] Unable to start RconServer. Error loading config:");
			e.printStackTrace();
			return;
		}
		
		HandlerCreator creator = new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				manager.setDebug(mConfig.debug);
				return new ServerLoginHandler(manager);
			}
			
			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				manager.setDebug(mConfig.debug);
				return new NetHandler(manager);
			}
		};
		
		IUserStore userstore = null;
		
		if(mConfig.store.equalsIgnoreCase("mysql")) {
			Properties props = new Properties();
			props.put("user",mConfig.databaseUsername);
			props.put("password",mConfig.databasePassword);
			props.put("useSSL",mConfig.databaseUseSSL);
			userstore = new MySQLUserStore(mConfig.databaseHost, mConfig.databaseName,props);

		}
		else
			userstore = new YamlUserStore(new File(pluginDir.toFile(), "users.yml"));
		
		String serverName = mConfig.serverName;
		if(serverName == null)
			serverName = "Proxy";
		
		installLogHandler();
		
		mServer = new VelocityRconServer(mConfig.port, serverName, userstore);
		
		loadWhitelist();

		// Load translations from messages.properties
		try (Stream<String> lines = loadResourceFile("com/velocitypowered/proxy/l10n/messages.properties")) {
			Key key = Key.key("velocity");
			TranslationRegistry registry = TranslationRegistry.create(key);

			lines.forEach(line -> {
				String[] parts = line.split("=", 2);
				if (parts.length == 2) {
					String translationKey = parts[0].trim();
					String translationValue = parts[1].trim();
					MessageFormat format = new MessageFormat(translationValue);
					registry.register(translationKey, Locale.ENGLISH, format);
				}
			});
		}
		
		try
		{
			logger.info("Starting RconServer on port " + mConfig.port);
			mServer.start(creator);
			mServer.openServer();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			mServer = null;
			return;
		}
		
		CommandMeta commandMeta = proxyServer.getCommandManager().metaBuilder("brcon").plugin(this).build();
		proxyServer.getCommandManager().register(commandMeta, new RconCommand());
	}

	private void installLogHandler()
	{
		logger.info("installLogHandler called");

		final org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

		Appender appender = new RemoteConsoleLogAppender("RemoteConsoleLogAppender", null, null, false, null);
		appender.start();
		if (appender.isStarted()) {
			System.out.println("[RemoteConsole] Log4j appender started");
			rootLogger.addAppender(appender);
		} else {
			System.out.println("[RemoteConsole] Log4j appender failed to start");
		}
	}
	
	public static String formatMessage(String message)
	{
		message = instance.mFormatter.format(new LogRecord(Level.INFO, message));
		if(message.endsWith("\n"))
			message = message.substring(0, message.length()-1);
		
		return message;
	}
	
	public boolean loadWhitelist()
	{
		File whitelist = new File(pluginDir.toFile(), "whitelist.txt");
		if (whitelist.exists())
		{
			try
			{
				mServer.getWhitelist().load(whitelist);
			}
			catch (IOException e)
			{
				logger.severe("Failed to load whitelist:");
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	public Stream<String> loadResourceFile(String filePath) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			System.out.println("File not found: " + filePath);
			return Stream.empty();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		return reader.lines();
	}
}
