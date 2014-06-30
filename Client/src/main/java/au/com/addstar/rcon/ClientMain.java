package au.com.addstar.rcon;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;

public class ClientMain
{
	private static void printUsage()
	{
		System.err.println("Better Remote Console version 1.0");
		System.err.println();
		System.err.println("Usage: <host> [options]");
		System.err.println();
		System.err.println("-p <port> Specifies port number. Defaults to 22050");
		System.err.println("-U <username> Specifies your username.");
		System.err.println("-P <password> Specifies password to use");
	}
	
	private static ClientMain mInstance;
	
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
		
		String host = args[0];
		
		int port = 22050;
		String username = "";
		String password = "";
		
		if(args.length > 1)
		{
			for(int i = 1; i < args.length - 1; i+= 2)
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
				case 'p':
					try
					{
						port = Integer.parseInt(args[i+1]);
						if(port <= 0 || port > 65535)
						{
							printUsage();
							return;
						}
					}
					catch(NumberFormatException e)
					{
						printUsage();
					}
					break;
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
		}
		
		mInstance = new ClientMain(new ConsoleScreen(), username, password);
		getConnectionManager().addConnection(host, port);
		mInstance.run();
	}
	
	private ConnectionManager mManager;
	private ConsoleScreen mConsole;
	private String mUsername;
	private String mPassword;
	
	private CountDownLatch mTabCompleteLatch; 
	private List<String> mTabCompleteResults;
	
	public ClientMain(ConsoleScreen screen, String username, String password)
	{
		mManager = new ConnectionManager();
		mConsole = screen;
		mUsername = username;
		mPassword = password;
	}
	
	public static ConnectionManager getConnectionManager()
	{
		return mInstance.mManager;
	}
	
	public void run()
	{
		try
		{
			mManager.connectAll(mUsername, mPassword);
			mUsername = mPassword = null;
			
			mConsole.start();
			
			mManager.waitUntilClosed();
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
	
	public static List<String> doTabComplete(String input) throws InterruptedException
	{
		if(mInstance.mManager.getActive() != null)
		{
			mInstance.mTabCompleteLatch = new CountDownLatch(1);
			mInstance.mManager.getActive().sendPacket(new PacketInTabComplete(input));
			mInstance.mTabCompleteLatch.await();
			return mInstance.mTabCompleteResults;
		}
		else
			return Collections.emptyList();
	}
	
	public static void onTabCompleteDone(List<String> data)
	{
		mInstance.mTabCompleteResults = data;
		mInstance.mTabCompleteLatch.countDown();
	}
}
