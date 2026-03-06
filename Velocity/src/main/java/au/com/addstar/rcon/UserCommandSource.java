package au.com.addstar.rcon;

import java.util.Locale;
import java.util.logging.Logger;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.util.Message;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

public class UserCommandSource implements ConsoleCommandSource
{
	private VelocityUser mUser;
	private ProxyServer proxyServer;
	private Logger logger;
	
	public UserCommandSource(VelocityUser user)
	{
		mUser = user;
		this.proxyServer = RemoteConsolePlugin.proxyServer;
		this.logger = RemoteConsolePlugin.logger;
	}
	
	public String getName()
	{
		return mUser.getName();
	}

	@Override
	public void sendRichMessage(String message)
	{
		if (message.contains("§")) {
			// Convert legacy message to string (can also include MiniMessage format)
			// Serialize method doesn't work if it contains legacy colour
			Component component = LegacyComponentSerializer.legacySection().deserialize(message);
			message = LegacyComponentSerializer.legacySection().serialize(component);
		} else {
			// Convert mini message format to string
			Component component = MiniMessage.miniMessage().deserialize(message);
			message = LegacyComponentSerializer.legacySection().serialize(component);
		}
		mUser.getManager().sendPacket(new PacketOutMessage(new Message(message, MessageType.Directed, logger.getName())));
	}

	@Override
	public void sendPlainMessage(@NotNull String message) {
		//logger.info("UCS/sendPlainMessage: " + mUser.getName() + " -> " + message);
		mUser.getManager().sendPacket(new PacketOutMessage(new Message(message, MessageType.Directed, logger.getName())));
	}

	@Override
	public void sendMessage(@NotNull ComponentLike message) {
		sendMessage(message.asComponent());
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		String msg;
		if (message instanceof TranslatableComponent) {
			String key = ((TranslatableComponent) message).key();
			TranslatableComponent tc = Component.translatable(key);
			Component c = GlobalTranslator.render(tc, Locale.getDefault());
			msg = LegacyComponentSerializer.legacySection().serialize(c);
		} else {
			msg = LegacyComponentSerializer.legacySection().serialize(message);
		}
		mUser.getManager().sendPacket(new PacketOutMessage(new Message(msg, MessageType.Directed, logger.getName())));
	}

	@Override
	public Tristate getPermissionValue(String s) {
		return RemoteConsolePlugin.proxyServer.getConsoleCommandSource().getPermissionValue(s);
	}
}
