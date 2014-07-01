package au.com.addstar.rcon;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.addstar.rcon.Event.EventType;
import au.com.addstar.rcon.command.CommandDispatcher;
import au.com.addstar.rcon.command.ExitCommand;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

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
	}
	
	private static ClientMain mInstance;
	public static int maxConsoleLines = 1000;
	
	public static void printMessage(String message)
	{
		mInstance.printMessage0(message);
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
		
		mInstance.run();
	}
	
	private ConnectionManager mManager;
	private ConsoleScreen mConsole;
	private String mUsername;
	private String mPassword;
	
	private CountDownLatch mTabCompleteLatch; 
	private List<String> mTabCompleteResults;
	
	private LinkedBlockingQueue<Event> mEventQueue;
	private CommandDispatcher mDispatcher;
	
	public ClientMain(ConsoleScreen screen, String username, String password)
	{
		mManager = new ConnectionManager();
		mConsole = screen;
		mUsername = username;
		mPassword = password;
		
		mEventQueue = new LinkedBlockingQueue<Event>();
		mDispatcher = new CommandDispatcher();
		registerCommands();
	}
	
	private void registerCommands()
	{
		mDispatcher.registerCommand(new ExitCommand());
	}
	
	public static ConnectionManager getConnectionManager()
	{
		return mInstance.mManager;
	}
	
	public static void handleCommand(ConsoleScreen screen, String command)
	{
		if(command.startsWith("."))
			mInstance.mDispatcher.dispatchCommand(screen, command);
		else if(mInstance.mManager.getActive() != null)
			mInstance.mManager.getActive().sendPacket(new PacketInCommand(command));
	}
	
	public static List<String> handleTabComplete(ConsoleScreen screen, String command)
	{
		if(command.startsWith("."))
			return mInstance.mDispatcher.tabComplete(screen, command);
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
	
	public void run()
	{
		try
		{
			mManager.connectAll(mUsername, mPassword);
			mUsername = mPassword = null;
			
			mConsole.start();
			
eventLoop:	while(true)
			{
				Event event = mEventQueue.take();
				
				switch(event.getType())
				{
				case ConnectionShutdown:
					event.<ClientConnection>getArgument().shutdown();
					break;
				case MessageUpdate:
				{
					ClientConnection connection = event.getArgument();
					if(connection == mManager.getActive())
					{
						connection.getMessageBuffer().update(mConsole, EnumSet.allOf(MessageType.class));
					}
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
	
	private void printMessage0(String message)
	{
		mConsole.printString(message);
	}
	
	public static void onTabCompleteDone(List<String> data)
	{
		mInstance.mTabCompleteResults = data;
		mInstance.mTabCompleteLatch.countDown();
	}
}
