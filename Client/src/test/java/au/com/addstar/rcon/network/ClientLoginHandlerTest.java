package au.com.addstar.rcon.network;

import au.com.addstar.rcon.NetHandler;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import au.com.addstar.rcon.util.CryptHelper;

import java.net.SocketException;
import java.security.KeyPair;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Running these Test requires the Test Server be instatiated on a different JVM first.
 *
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/02/2018.
 */
public class ClientLoginHandlerTest {

    private NetworkManager manager;
    private ClientLoginHandler handler;
    private ClientConnection connection;

    public void setup() {
        HandlerCreator creator = new HandlerCreator() {
            @Override
            public INetworkHandler newHandlerLogin(NetworkManager manager) {
                manager.setDebug(true);
                return new ClientLoginHandler(manager);
            }

            @Override
            public INetworkHandler newHandlerMain(NetworkManager manager) {
                manager.setDebug(true);
                return new NetHandler(manager);
            }
        };
        manager = new NetworkManager(creator);
        handler = new ClientLoginHandler(manager);
        handler.setLoginInfo("TestUser0", "password");
        connection = new ClientConnection("localhost", 22000);
        try {
            connection.connect(creator);
        } catch (SocketException | InterruptedException e) {
            e.printStackTrace();
        }
        handler.setClientConnection(connection);
    }

    /**
     * Test is not annotated as it requires the Test Server to be run.
     */
    public void handleEncryptStart() {
        assertTrue(connection.getManager().getConnectionState() == ConnectionState.Login);
        connection.startLogin("TestUser0","password");
        try {
            if (connection.waitForLogin()) {
                assertTrue(connection.getManager().getConnectionState() == ConnectionState.Main);
            }else{
                assertTrue(connection.getManager().getConnectionState() == ConnectionState.Main);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        byte[] mBlob = new byte[14];
        Random mRand = new Random();
        mRand.nextBytes(mBlob);
        KeyPair serverkey = CryptHelper.generateKey();
        assertNotNull(serverkey);
        PacketOutEncryptStart packet = new PacketOutEncryptStart(serverkey.getPublic(),mBlob);
        packet.handlePacket(handler);
    }
}