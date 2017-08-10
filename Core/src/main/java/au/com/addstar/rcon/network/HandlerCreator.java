package au.com.addstar.rcon.network;

import au.com.addstar.rcon.network.handlers.INetworkHandler;


public interface HandlerCreator
{
	INetworkHandler newHandlerLogin(NetworkManager manager);
	INetworkHandler newHandlerMain(NetworkManager manager);
}
