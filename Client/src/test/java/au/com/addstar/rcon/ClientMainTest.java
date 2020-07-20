package au.com.addstar.rcon;

import au.com.addstar.rcon.network.ClientConnection;
import au.com.addstar.rcon.objects.TestConsole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static au.com.addstar.rcon.ClientMain.getConnectionManager;
import static au.com.addstar.rcon.ClientMain.mInstance;
import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/02/2018.
 */
public class ClientMainTest {

    private BungeeRconServer server;
    private ConsoleScreen screen;
    @Before
    public void setUp() throws Exception {
        screen = new TestConsole();
        mInstance = new ClientMain(screen, "TestUser0", "password", true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void main() {
        assertNotNull(mInstance);
        assertNotNull(getConnectionManager());
        getConnectionManager().addConnection("localhost", 22000, "TestServer");
        ClientConnection connection = getConnectionManager().getConnection("TestServer");
        assertNull(connection);
        try {
            getConnectionManager().connectAll();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        screen.printString("This is a test message");
       // mInstance.run();
        //connection = getConnectionManager().getConnection("TestServer");
        //assertTrue(connection.isLoggedIn());
    }
}