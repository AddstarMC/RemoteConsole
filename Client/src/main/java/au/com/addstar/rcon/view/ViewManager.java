package au.com.addstar.rcon.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.IConnectionListener;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.util.Message;

public class ViewManager
{
	public static final SystemOnlyConsoleView systemView = new SystemOnlyConsoleView();
	private ConsoleView mActiveView;
	private HashMap<String, ConsoleView> mViews;
	
	public ViewManager()
	{
		mViews = new HashMap<String, ConsoleView>();
		mActiveView = systemView;
	}
	
	public synchronized void addView(String name, ConsoleView view)
	{
		if(name.equalsIgnoreCase("system"))
			throw new IllegalArgumentException("System console cannot be overridden");
		
		mViews.put(name.toLowerCase(), view);
		view.setName(name);
		if(view instanceof IConnectionListener)
			ClientMain.registerConnectionListener((IConnectionListener)view);
	}
	
	public synchronized ConsoleView getView(String name)
	{
		return mViews.get(name.toLowerCase());
	}
	
	public synchronized Set<String> getViewNames()
	{
		return Collections.unmodifiableSet(mViews.keySet());
	}
	
	public synchronized void removeView(String name) throws IllegalArgumentException
	{
		ConsoleView view = mViews.remove(name.toLowerCase());
		if(view == null)
			throw new IllegalArgumentException("Unknown view " + name);
		
		if(view instanceof IConnectionListener)
			ClientMain.deregisterConnectionListener((IConnectionListener)view);
		
		if(mActiveView == view)
		{
			mActiveView = systemView;
			if(!mViews.isEmpty())
			{
				// Try to use one that handles the current connection
				ClientConnection activeCon = ClientMain.getConnectionManager().getActive();
				ConsoleView first = null;
				for(ConsoleView otherView : mViews.values())
				{
					if(first == null)
						first = otherView;
					
					if(otherView.isHandling(activeCon))
					{
						mActiveView = otherView;
						break;
					}
				}
				
				// Use the first one we found
				if(mActiveView == systemView)
					mActiveView = first;
			}
			
			mActiveView.getBuffer().display(ClientMain.getConsole(), mActiveView.getFilter());
		}
	}
	
	public synchronized void setActive(String name) throws IllegalArgumentException
	{
		if(name == null)
			mActiveView = systemView;
		else
		{
			ConsoleView view = getView(name);
			if(view == null)
				throw new IllegalArgumentException("Unknown view " + name);
			
			mActiveView = view;
		}
		
		mActiveView.getBuffer().display(ClientMain.getConsole(), mActiveView.getFilter());
	}
	
	public ConsoleView getActive()
	{
		return mActiveView;
	}
	
	public synchronized void addMessage(ClientConnection from, Message message)
	{
		for(ConsoleView view : mViews.values())
		{
			if(view.isHandling(from))
				view.getBuffer().addMessage(message);
		}
	}
	
	public synchronized void addSystemMessage(String message)
	{
		systemView.getBuffer().addMessage(new Message(message, MessageType.System, "RemoteConsole"));
		
		for(ConsoleView view : mViews.values())
			view.getBuffer().addMessage(new Message(message, MessageType.System, "RemoteConsole"));
		
		mActiveView.getBuffer().update(ClientMain.getConsole(), mActiveView.getFilter());
	}
	
	public void update(ClientConnection connection)
	{
		if(mActiveView.isHandling(connection))
			mActiveView.getBuffer().update(ClientMain.getConsole(), mActiveView.getFilter());
	}
}
