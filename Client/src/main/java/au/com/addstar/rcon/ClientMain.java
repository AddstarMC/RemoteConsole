package au.com.addstar.rcon;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.ConnectException;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.ConnectionState;
import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.login.PacketInLoginBegin;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;

public class ClientMain implements GenericFutureListener<Future<? super Void>>
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
		
		mInstance = new ClientMain(new ClientConnection(host, port), new ConsoleScreen(), username, password);
		mInstance.run();
	}
	
	private ClientConnection mConnection;
	private ConsoleScreen mConsole;
	private String mUsername;
	private String mPassword;
	
	private boolean mRunning;
	
	public ClientMain(ClientConnection connection, ConsoleScreen screen, String username, String password)
	{
		mConnection = connection;
		mConsole = screen;
		mUsername = username;
		mPassword = password;
	}
	
	public void run()
	{
		HandlerCreator creator = new HandlerCreator()
		{
			@Override
			public INetworkHandler newHandlerLogin( NetworkManager manager )
			{
				return new ClientLoginHandler(manager);
			}
			
			@Override
			public INetworkHandler newHandlerMain( NetworkManager manager )
			{
				return null;
			}
		};
		
		try
		{
			mConnection.run(creator);
			mConnection.addTerminationListener(this);
		
			mRunning = true;
			NetworkManager manager = mConnection.getManager();
			
			manager.waitForActive();
			mConsole.setNetworkHandler(manager);
			
			((ClientLoginHandler)manager.getNetHandler()).setLoginInfo(mUsername, mPassword);
			mUsername = mPassword = null;
			
			mConnection.sendPacket(new PacketInLoginBegin());
			
			while(mRunning)
			{
				if(manager.getConnectionState() != ConnectionState.Main)
				{
					Thread.sleep(100);
					continue;
				}
				
				String command = mConsole.readLine();
				
				if(command != null) // Handle commands
				{
					if(command.equals("exit"))
					{
						manager.close("Quitting");
						break;
					}
					
					mConnection.sendPacket(new PacketInCommand(command));
				}
			}
			
			mConnection.shutdown();
		}
		catch(InterruptedException e)
		{
			System.err.println(e.getMessage());
		}
		catch(ConnectException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	private void printMessage0(String message)
	{
		mConsole.printString(message);
	}
	
	@Override
	public void operationComplete( Future<? super Void> future ) throws Exception
	{
		String message = mConnection.getManager().getDisconnectReason();
		
		if(message != null)
			mConsole.printString(message);
		
		mRunning = false;
	}
}
