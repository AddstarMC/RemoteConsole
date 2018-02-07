package au.com.addstar.rcon;

import au.com.addstar.rcon.objects.TestServer;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/02/2018.
 */
public class ServerTest {

    public ServerTest() {
    }

    /**
     * Created for use for the Add5tar MC Minecraft server
     * Created by benjamincharlton on 5/02/2018.
     */
    public static void main(String[] args){

        Thread thread = new Thread(() -> {
            new TestServer(22000, "TestServer", new UserStore());
        });
        thread.start();
    }
}

