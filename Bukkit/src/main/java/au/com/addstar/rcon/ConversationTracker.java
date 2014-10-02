package au.com.addstar.rcon;

import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;

/**
 * This is ripped from CraftBukkit to avoid the version issue
 */
public class ConversationTracker
{
	public ConversationTracker()
	{
	}

	private LinkedList<Conversation> conversationQueue = new LinkedList<Conversation>();

	public synchronized boolean beginConversation( Conversation conversation )
	{
		if ( !conversationQueue.contains(conversation) )
		{
			conversationQueue.addLast(conversation);
			if ( conversationQueue.getFirst() == conversation )
			{
				conversation.begin();
				conversation.outputNextPrompt();
				return true;
			}
		}
		return true;
	}

	public synchronized void abandonConversation( Conversation conversation, ConversationAbandonedEvent details )
	{
		if ( !conversationQueue.isEmpty() )
		{
			if ( conversationQueue.getFirst() == conversation )
			{
				conversation.abandon(details);
			}
			if ( conversationQueue.contains(conversation) )
			{
				conversationQueue.remove(conversation);
			}
			if ( !conversationQueue.isEmpty() )
			{
				((Conversation) conversationQueue.getFirst()).outputNextPrompt();
			}
		}
	}

	public synchronized void abandonAllConversations()
	{
		LinkedList<Conversation> oldQueue = conversationQueue;
		conversationQueue = new LinkedList<Conversation>();
		for (Conversation conversation : oldQueue)
		{
			try
			{
				conversation.abandon(new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
			}
			catch ( Throwable t )
			{
				Bukkit.getLogger().log(Level.SEVERE, "Unexpected exception while abandoning a conversation", t);
			}
		}
	}

	public synchronized void acceptConversationInput( String input )
	{
		if ( isConversing() )
		{
			((Conversation) conversationQueue.getFirst()).acceptInput(input);
		}
	}

	public synchronized boolean isConversing()
	{
		return !conversationQueue.isEmpty();
	}

	public synchronized boolean isConversingModaly()
	{
		return (isConversing()) && (((Conversation) conversationQueue.getFirst()).isModal());
	}
}
