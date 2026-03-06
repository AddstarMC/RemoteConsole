package au.com.addstar.rcon;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerServer;
import au.com.addstar.rcon.network.packets.main.*;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;
import au.com.addstar.rcon.server.auth.StoredPassword;
import au.com.addstar.rcon.util.Message;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NetHandler extends AbstractNetworkHandler implements INetworkMainHandlerServer
{
	public NetHandler(NetworkManager manager)
	{
		super(manager);
	}
	
	@Override
	public void handleCommand( final PacketInCommand packet )
	{
        if (packet.command == null || packet.command.strip().isEmpty())
            return;

		RemoteConsolePlugin.proxyServer.getScheduler().buildTask(RemoteConsolePlugin.instance, () -> {
            VelocityUser user = (VelocityUser)((ServerNetworkManager)getManager()).getUser();
            //RemoteConsolePlugin.logger.info("NetHandler/handleCommand: " + user.getName() + " -> " + packet.command);
            if (user.isRestricted())
            {
                if (!RconServer.instance.getWhitelist().isWhitelisted(packet.command))
                {
                    user.asCommandSender().sendRichMessage("<red>You are not permitted to use that command");
                    return;
                }
            }

            CommandManager cm = RemoteConsolePlugin.proxyServer.getCommandManager();
            CommandMeta meta = cm.getCommandMeta(packet.command.split(" ")[0]);
            if (meta == null) {
                user.asCommandSender().sendRichMessage("<red>Command not found");
                return;
            /*} else {
                RemoteConsolePlugin.logger.info("Valid command: "
                        + packet.command
                        + " (" + meta.getAliases() + ")");*/
            }

            CompletableFuture<Boolean> cmd = cm.executeAsync(user.asUserCommandSender(), packet.command);
            try {
                cmd.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }).schedule();
	}

	@Override
	public void handleTabComplete( final PacketInTabComplete packet )
	{
		RemoteConsolePlugin.proxyServer.getScheduler().buildTask(RemoteConsolePlugin.instance, () -> {
            VelocityUser user = (VelocityUser)((ServerNetworkManager)getManager()).getUser();

            if (user.isRestricted())
            {
                if (!RconServer.instance.getWhitelist().isWhitelisted(packet.message))
                {
                    getManager().sendPacket(new PacketOutTabComplete(Collections.<String>emptyList()));
                    return;
                }
            }

            //CompletableFuture<Boolean> results = RemoteConsolePlugin.proxyServer.getCommandManager().executeAsync(user.asCommandSender(), packet.message);
            getManager().sendPacket(new PacketOutTabComplete(new ArrayList<>()));

        }).schedule();
	}
	
	@Override
	public void handlePassword( final PacketInPassword packet )
	{
        RemoteConsolePlugin.proxyServer.getScheduler().buildTask(RemoteConsolePlugin.instance, () -> {
            VelocityUser user = (VelocityUser)((ServerNetworkManager)getManager()).getUser();
            StoredPassword current = user.getPassword();

            if (current.matches(packet.previousPassword))
            {
                user.setPassword(StoredPassword.generate(packet.newPassword));
                if(!RconServer.instance.saveUser(user))
                    getManager().sendPacket(new PacketOutMessage(new Message("<red>Unable to save changes, an internal error occured.", MessageType.Directed, "RemoteConsole")));
                else
                    getManager().sendPacket(new PacketOutMessage(new Message("<yellow>Your password has been updated.", MessageType.Directed, "RemoteConsole")));
            }
            else
                getManager().sendPacket(new PacketOutMessage(new Message("<red>Your password did not match your current password", MessageType.Directed, "RemoteConsole")));
        }).schedule();
	}
}
