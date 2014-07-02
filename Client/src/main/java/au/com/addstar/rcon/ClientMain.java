package au.com.addstar.rcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.command.CommandDispatcher;
import au.com.addstar.rcon.command.ConnectCommand;
import au.com.addstar.rcon.command.CreateViewCommand;
import au.com.addstar.rcon.command.DisconnectCommand;
import au.com.addstar.rcon.command.ExitCommand;
import au.com.addstar.rcon.command.FilterCommand;
import au.com.addstar.rcon.command.RemoveViewCommand;
import au.com.addstar.rcon.command.ServersCommand;
import au.com.addstar.rcon.command.SwitchCommand;
import au.com.addstar.rcon.command.ViewCommand;
import au.com.addstar.rcon.command.ViewsCommand;
import au.com.addstar.rcon.command.WhoCommand;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.view.SingleConsoleView;
import au.com.addstar.rcon.view.ViewManager;

public class ClientMain
{
	private static void printUsage()
	{
		System.err.println("Better Remote Console version 1.0");
		System.err.println();
		System.err.println("Usage: <host[:port]> <host[:port]> ... [options]");
		System.err.println();
		System.err.println("-U <username> Specifies your username.");
		System.err.println("-P <password> Specifies password to use");
		System.err.println("-c <configfile> Specifies a config file to load");
	}
	
	private static ClientMain mInstance;
	public static int maxConsoleLines = 1000;
	
	public static void printMessage(String message)
	{
		mInstance.mConsole.printString(message);
	}
	
	public static void printErrMessage(String message)
	{
		mInstance.mConsole.printErrString(message);
	}
	
	public static void main(String[] args) throws Exception
	{
		if(args.length < 1)
		{
			printUsage();
			return;
		}
		
		int hostPart = -1;

		String username = "";
		String password = "";
		String config = null;
		
		for(hostPart = 0; hostPart < args.length; ++hostPart)
		{
			if(args[hostPart].startsWith("-"))
				break;
		}
			
		for(int i = hostPart; i < args.length - 1; i+= 2)
		{
			if(!args[i].startsWith("-") || args[i].length() == 1)
			{
				System.out.println("Unknown option: " + args[i]);
				printUsage();
				return;
			}
			
			char opt = args[i].charAt(1);
			
			switch(opt)
			{
			case 'P':
				password = args[i+1];
				break;
			case 'U':
				username = args[i+1];
				break;
			case 'c':
				config = args[i+1];
				break;
			default:
				System.out.println("Unknown option: " + args[i]);
				printUsage();
				return;
			}
		}
		
		mInstance = new ClientMain(new ConsoleScreen(), username, password);
		
		for(int i = 0; i < hostPart; ++i)
		{
			String fullHost = args[i];
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
						return;
					}
				}
				catch(NumberFormatException e)
				{
					printUsage();
				}
			}
			else
				host = fullHost;
			
			getConnectionManager().addConnection(host, port);
		}
		
		if(config != null)
		{
			ConfigLoader.loadConfig(new File(config));
		}
		
		mInstance.run();
	}
	
	private ConnectionManager mManager;
	private ConsoleScreen mConsole;
	private ViewManager mViewManager;
	private ArrayList<IConnectionListener> mConnectionListeners;
	
	private CountDownLatch mTabCompleteLatch; 
	private List<String> mTabCompleteResults;
	
	private LinkedBlockingQueue<Event> mEventQueue;
	private CommandDispatcher mDispatcher;
	
	public ClientMain(ConsoleScreen screen, String username, String password)
	{
		mManager = new ConnectionManager(username, password);
		mConsole = screen;
		
		mEventQueue = new LinkedBlockingQueue<Event>();
		mDispatcher = new CommandDispatcher();
		mViewManager = new ViewManager();
		mConnectionListeners = new ArrayList<IConnectionListener>();
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
		if(mViewManager.getActive() == ViewManager.nullView || mViewManager.getActive() instanceof SingleConsoleView)
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
