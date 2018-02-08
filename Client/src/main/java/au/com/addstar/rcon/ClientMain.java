package au.com.addstar.rcon;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.command.*;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.view.SingleConsoleView;
import au.com.addstar.rcon.view.ViewManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientMain
{
	private static void printUsage(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		String header = "Remote Console version 1.0.0\n" + 
						"Connects remotely to supported consoles allowing you to see and interact with them." + 
						"Hosts can either be specified as 'hostname' or 'hostname:port'\n\n";
		
		formatter.printHelp("rcon [OPTIONS]... [HOST]...", header, options, "", false);
	}
	
	protected static ClientMain mInstance;
	public static int maxConsoleLines = 1000;
	public static boolean showPrompt = true;
	
	@SuppressWarnings( "static-access" )
	public static void main(String[] args) throws Exception
	{
		Options options = new Options();
		
		options.addOption(OptionBuilder.withArgName("USERNAME")
							.hasArg()
							.withDescription("Specifies your username")
							.withLongOpt("username")
							.create('u'));
		
		options.addOption(OptionBuilder.withArgName("PASSWORD")
				.hasArg()
				.withDescription("Specifies your password")
				.withLongOpt("password")
				.create('p'));
		
		options.addOption(OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("Specifies a config file to load")
				.withLongOpt("config")
				.create('c'));
		options.addOption(OptionBuilder.withArgName("DEBUG")
				.hasArg()
				.withDescription("Enables Debugging")
				.withLongOpt("debug")
				.create('d'));
		options.addOption("n", "no-prompt", false, "Disables the prompt, useful for piping the output");
		
		options.addOption(OptionBuilder.withArgName("COUNT")
				.hasArg()
				.withDescription("Sets the maximum number of lines to show. Default: 1000")
				.withLongOpt("max-lines")
				.withType(Integer.valueOf(1000))
				.create('c'));
		
		BasicParser parser = new BasicParser();
		CommandLine cl = null;
		try
		{
			cl = parser.parse(options, args, true);
			UserSettings settings = UserSettings.load();
			
			// Load options
			String username = null;
			String password = null;
			boolean debug = false;
			if(cl.hasOption('u'))
				username = cl.getOptionValue('u');
			if(cl.hasOption('p'))
				password = cl.getOptionValue('p');
			
			String config = null;
			if(cl.hasOption('c'))
				config = cl.getOptionValue('c');
			
			if(cl.hasOption('n'))
				showPrompt = false;
			
			if(cl.hasOption('l'))
				maxConsoleLines = (Integer)cl.getParsedOptionValue("l");
			if(cl.hasOption('d'))
				debug = true;

			ConsoleScreen screen = new ConsoleScreen();
			screen.setDebug(debug);

			if(settings != null && username == null && password == null)
			{
				username = settings.username;
				password = settings.password;
			}
			
			if(username == null)
				username = screen.readUsername();
			
			if(password == null)
				password = screen.readPassword();
			
			if(username == null)
			{
				System.err.println("No username supplied. Unable to start.");
				return;
			}
			
			if(password == null)
			{
				System.err.println("No password supplied. Unable to start.");
				return;
			}
			
			// Init
			mInstance = new ClientMain(screen, username, password,debug);
			
			// Load hosts
			for(String fullHost : cl.getArgs())
			{
				String host;
				int port = 22050;
				
				if(fullHost.contains(":"))
				{
					String[] split = fullHost.split(":");
					host = split[0];
					
					try
					{
						port = Integer.parseInt(split[1]);
						if(port <= 0 || port > 65535)
						{
							System.err.println("Port number in " + fullHost + " is out of range");
							printUsage(options);
							return;
						}
					}
					catch(NumberFormatException e)
					{
						System.err.println("Port number in " + fullHost + " is not a number");
						printUsage(options);
					}
				}
				else
					host = fullHost;
				
				getConnectionManager().addConnection(host, port, null);
			}
			
			if(config != null)
			{
				try
				{
					ConfigLoader.loadConfig(new File(config));
				}
				catch(FileNotFoundException e)
				{
					System.out.println("Cannot find config " + config);
					return;
				}
				catch(IOException e)
				{
					System.out.println("Failed to load config " + config + ": " + e.getMessage());
					return;
				}
			}
			
			mInstance.run();
		}
		catch(ParseException e)
		{
			System.out.println(e.getMessage());
			printUsage(options);
		}
	}
	
	private ConnectionManager mManager;
	private ConsoleScreen mConsole;
	private ViewManager mViewManager;
	private ArrayList<IConnectionListener> mConnectionListeners;
	private boolean debug;
	private CountDownLatch mTabCompleteLatch; 
	private List<String> mTabCompleteResults;
	private Logger logger;
	
	private LinkedBlockingQueue<Event> mEventQueue;
	private CommandDispatcher mDispatcher;
	
	public ClientMain(ConsoleScreen screen, String username, String password, boolean debug)
	{
		this.debug = debug;
		logger = LoggerFactory.getLogger(this.getClass());
		mManager = new ConnectionManager(username, password,this.debug);
		mConsole = screen;
		mEventQueue = new LinkedBlockingQueue<>();
		mDispatcher = new CommandDispatcher();
		mViewManager = new ViewManager();
		mConnectionListeners = new ArrayList<>();
		registerCommands();

	}
	
	private void registerCommands()
	{
		mDispatcher.registerCommand(new ExitCommand());
		mDispatcher.registerCommand(new ServersCommand());
		mDispatcher.registerCommand(new SwitchCommand());
		mDispatcher.registerCommand(new ConnectCommand());
		mDispatcher.registerCommand(new DisconnectCommand());
		mDispatcher.registerCommand(new WhoCommand());
		
		mDispatcher.registerCommand(new CreateViewCommand());
		mDispatcher.registerCommand(new RemoveViewCommand());
		mDispatcher.registerCommand(new ViewCommand());
		mDispatcher.registerCommand(new ViewsCommand());
		mDispatcher.registerCommand(new FilterCommand());
		mDispatcher.registerCommand(new PasswordCommand());
	}
	
	public static ConnectionManager getConnectionManager()
	{
		return mInstance.mManager;
	}
	
	public static ViewManager getViewManager()
	{
		return mInstance.mViewManager;
	}
	
	public static void handleCommand(ConsoleScreen screen, String command)
	{
		if(command.startsWith("."))
			mInstance.mEventQueue.add(new Event(EventType.Command, command));
		else if(mInstance.mManager.getActive() != null)
			mInstance.mManager.getActive().sendPacket(new PacketInCommand(command));
	}
	
	public static List<String> handleTabComplete(ConsoleScreen screen, String command)
	{
		if(command.startsWith("."))
			return mInstance.mDispatcher.tabComplete(screen, command.substring(1));
		else if(mInstance.mManager.getActive() != null)
		{
			try
			{
				mInstance.mTabCompleteLatch = new CountDownLatch(1);
				mInstance.mManager.getActive().sendPacket(new PacketInTabComplete(command));
				mInstance.mTabCompleteLatch.await();
				return mInstance.mTabCompleteResults;
			}
			catch(InterruptedException e)
			{
				return Collections.emptyList();
			}
		}
		else
			return Collections.emptyList();
	}
	
	public static void callEvent(Event event)
	{
		mInstance.mEventQueue.add(event);
	}
	
	public static void registerConnectionListener(IConnectionListener listener)
	{
		mInstance.mConnectionListeners.add(listener);
	}
	
	public static void deregisterConnectionListener(IConnectionListener listener)
	{
		mInstance.mConnectionListeners.remove(listener);
	}
	
	public void run()
	{
		try
		{
			mManager.connectAll();
			
			mConsole.start();
			
eventLoop:	while(true)
			{
				Event event = mEventQueue.take();
				
				switch(event.getType())
				{
				case ConnectionStart:
				{
					ClientConnection connection = event.getArgument();
					mViewManager.addView("*" + connection.getId(), new SingleConsoleView(connection));
					for(IConnectionListener listener : mConnectionListeners)
						listener.connectionJoin(connection);
					break;
				}
				case ConnectionShutdown:
				{
					ClientConnection connection = event.getArgument();
					mViewManager.removeView("*" + connection.getId());
					if(mManager.getActive() == connection)
					{
						mManager.switchActive(null);
						for(String id : mManager.getConnectionNames())
						{
							if(!id.equals(connection.getId()))
							{
								mManager.switchActive(id);
								break;
							}
						}
					}
					
					for(IConnectionListener listener : mConnectionListeners)
						listener.connectionEnd(connection);
					
					connection.shutdown();
					break;
				}
				case MessageUpdate:
				{
					ClientConnection connection = event.getArgument();
					mViewManager.update(connection);
					break;
				}
				case Command:
				{
					mDispatcher.dispatchCommand(mConsole, event.<String>getArgument().substring(1));
					break;
				}
				case ActiveServerChange:
				{
					ClientConnection connection = event.getArgument();
					onSwitchActive(connection);
					break;
				}
				case Quit:
					break eventLoop;
				}
				
			}
			
			mManager.closeAll("Quitting");
			mManager.waitUntilClosed();
			
			// Shutdown all remaining threads
			while(!mEventQueue.isEmpty())
			{
				Event event = mEventQueue.take();
				if(event.getType() == EventType.ConnectionShutdown)
					event.<ClientConnection>getArgument().shutdown();
			}
		}
		catch(InterruptedException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	private void onSwitchActive(ClientConnection active)
	{
		if(mViewManager.getActive() == ViewManager.systemView || mViewManager.getActive() instanceof SingleConsoleView)
		{
			if(active == null)
				mViewManager.setActive(null);
			else
				mViewManager.setActive("*" + active.getId());
		}
	}
	
	public static void onTabCompleteDone(List<String> data)
	{
		mInstance.mTabCompleteResults = data;
		mInstance.mTabCompleteLatch.countDown();
	}

	public static ConsoleScreen getConsole()
	{
		return mInstance.mConsole;
	}
}
