package au.com.addstar.rcon.network;


public interface HandlerCreator
{
	public NetworkHandler newHandler(NetworkManager manager);
}
