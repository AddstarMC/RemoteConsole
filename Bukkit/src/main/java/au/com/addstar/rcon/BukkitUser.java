package au.com.addstar.rcon;


import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.Level;

import org.bukkit.command.ConsoleCommandSender;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.util.Message;

public class BukkitUser extends User {
	private UserCommandSender mSender;

	private static final ImmutableMap<Level, java.util.logging.Level> LEVELS =
			new ImmutableMap.Builder<Level, java.util.logging.Level>()
					.put(Level.ALL, java.util.logging.Level.ALL)
					.put(Level.DEBUG, java.util.logging.Level.FINE)
					.put(Level.ERROR, java.util.logging.Level.SEVERE)
					.put(Level.FATAL, java.util.logging.Level.SEVERE)
					.put(Level.INFO, java.util.logging.Level.INFO)
					.put(Level.OFF, java.util.logging.Level.OFF)
					.put(Level.TRACE, java.util.logging.Level.FINEST)
					.put(Level.WARN, java.util.logging.Level.WARNING)
					.build();

	public BukkitUser(String name) {
		super(name);
	}

	public ConsoleCommandSender asCommandSender() {
		return mSender;
	}

	@Override
	public void setManager(NetworkManager manager) {
		super.setManager(manager);

		if (manager == null) {
			mSender.onRemove();
			mSender = null;
		} else
			mSender = new UserCommandSender(this);
	}

	public void sendMessage(String message, MessageType type) {
		mSender.sendMessage(message, type);
	}

	public void sendLog(LogEvent event) {
		java.util.logging.Level level = LEVELS.get(event.getLevel());
		if (level == null) level = java.util.logging.Level.FINE;
//		switch(event.getLevel())
//		{
//		case ALL:
//			level = Level.ALL;
//			break;
//		case DEBUG:
//			level = Level.FINEST;
//			break;
//		case ERROR:
//			level = Level.SEVERE;
//			break;
//		case FATAL:
//			level = Level.SEVERE;
//			break;
//		case INFO:
//			level = Level.INFO;
//			break;
//		case OFF:
//			level = Level.OFF;
//			break;
//		case TRACE:
//			level = Level.FINER;
//			break;
//		case WARN:
//			level = Level.WARNING;
//			break;
//		default:
//			level = Level.FINE;
//			break;
//		}

		MessageType type = MessageType.Log;
		if (event.getThrown() != null)
			type = MessageType.Exception;
		else if (event.getThreadName().startsWith("Async Chat Thread"))
			type = MessageType.Chat;

		long time = event.getTimeMillis();
		String thread = event.getThreadName();
		String message = event.getMessage().getFormattedMessage();

		PacketOutMessage packet = new PacketOutMessage(new Message(message, type, time, level, thread, event.getLoggerName()));
		getManager().sendPacket(packet);
	}

}
