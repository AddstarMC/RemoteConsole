package au.com.addstar.rcon.objects;

import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.AbstractNetworkHandler;
import au.com.addstar.rcon.network.handlers.INetworkMainHandlerServer;
import au.com.addstar.rcon.network.packets.main.PacketInCommand;
import au.com.addstar.rcon.network.packets.main.PacketInPassword;
import au.com.addstar.rcon.network.packets.main.PacketInTabComplete;
import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import au.com.addstar.rcon.server.ServerNetworkManager;
import au.com.addstar.rcon.server.auth.StoredPassword;
import au.com.addstar.rcon.util.Message;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/02/2018.
 */
public class NetTestHandler extends AbstractNetworkHandler implements INetworkMainHandlerServer {

    public NetTestHandler(NetworkManager manager) {
        super(manager);
    }

    @Override
    public void handleCommand(PacketInCommand packet) {
    }

    @Override
    public void handleTabComplete(PacketInTabComplete packet) {
    }

    @Override
    public void handlePassword(PacketInPassword packet) {
        Thread t = new Thread() {
            @Override
            public void run() {
                TestUser user = (TestUser) ((ServerNetworkManager) getManager()).getUser();
                StoredPassword current = user.getPassword();
                if (current.matches(packet.previousPassword)) {
                    user.setPassword(StoredPassword.generate(packet.newPassword));
                    if (!TestServer.getInstance().saveUser(user)) {
                        System.out.println("Unable to save changes, an internal error occured.");
                    } else {
                        getManager().sendPacket(new PacketOutMessage(new Message("Your password has been updated.", PacketOutMessage.MessageType.Directed, "RemoteConsole")));
                    }
                }
            }

        };
    }
}
