package au.com.addstar.rcon.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import au.com.addstar.rcon.ClientMain;
import au.com.addstar.rcon.IConnectionListener;
import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;

public class ViewManager
{
	public static final NullConsoleView nullView = new NullConsoleView();
	private ConsoleView mActiveView;
	private HashMap<String, ConsoleView> mViews;
	
	public ViewManager()
	{
		mViews = new HashMap<String, ConsoleView>();
		mActiveView = nullView;
	}
	
	public synchronized void addView(String name, ConsoleView view)
	{
		mViews.put(name.toLowerCase(), view);
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
			mActiveView = nullView;
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
				if(mActiveView == nullView)
					mActiveView = first;
			}
			
			mActiveView.getBuffer().display(ClientMain.getConsole(), mActiveView.getFilter());
		}
	}
	
	public synchronized void setActive(String name) throws IllegalArgumentException
	{
		if(name == null)
			mActiveView = nullView;
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
	
	public synchronized void addMessage(ClientConnection from, String message, MessageType type)
	{
		for(ConsoleView view : mViews.values())
		{
			if(view.isHandling(from))
				view.getBuffer().addMessage(view.getPrefix(from, type) + message + view.getSuffix(from, type), type);
		}
	}
	
	public void update(ClientConnection connection)
	{
		if(mActiveView.isHandling(connection))
			mActiveView.getBuffer().update(ClientMain.getConsole(), mActiveView.getFilter());
	}
}
