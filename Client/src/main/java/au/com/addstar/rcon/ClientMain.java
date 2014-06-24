package au.com.addstar.rcon;

import java.net.ConnectException;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.PacketInCommand;

public class ClientMain
{
	private static ClientConnection mConnection;
	
	public static void main(String[] args) throws Exception
	{
		mConnection = new ClientConnection("localhost", 22050);
		try
		{
			mConnection.run();
		}
		catch(ConnectException e)
		{
			System.out.println(e.getMessage());
			return;
		}
		
		mConnection.sendPacket(new PacketInCommand("test"));
		System.out.println("Sleeping");
		Thread.sleep(2000);
		System.out.println("Shutting down");
		mConnection.shutdown();
		System.out.println("Shut down");
	}
}
