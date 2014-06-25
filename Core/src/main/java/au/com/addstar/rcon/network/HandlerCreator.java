package au.com.addstar.rcon.network;

import au.com.addstar.rcon.network.handlers.INetworkHandler;


public interface HandlerCreator
{
	public INetworkHandler newHandlerLogin(NetworkManager manager);
	public INetworkHandler newHandlerMain(NetworkManager manager);
}
