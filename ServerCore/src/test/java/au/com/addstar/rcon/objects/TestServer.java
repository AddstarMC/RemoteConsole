package au.com.addstar.rcon.objects;

import au.com.addstar.rcon.network.HandlerCreator;
import au.com.addstar.rcon.network.NetworkManager;
import au.com.addstar.rcon.network.handlers.INetworkHandler;
import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerLoginHandler;
import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.server.auth.IUserStore;
import au.com.addstar.rcon.server.auth.StoredPassword;

import java.io.IOException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/02/2018.
 */
public class TestServer extends RconServer {

    public static TestServer getInstance() {
        return instance;
    }

    private static TestServer instance;
    private String mConsoleFormat;

    public  TestServer(int port, String name, IUserStore userstore) {
        super(port, name, userstore);
        createUser("TestUser0", StoredPassword.generate("password"),false);
        createUser("TestUser1", StoredPassword.generate("password"),false);
        HandlerCreator creator = new HandlerCreator()
        {
            @Override
            public INetworkHandler newHandlerLogin(NetworkManager manager )
            {
                manager.setDebug(true);
                return new ServerLoginHandler(manager);
            }

            @Override
            public INetworkHandler newHandlerMain( NetworkManager manager )
            {
                manager.setDebug(true);
                return new NetTestHandler(manager);
            }
        };
        instance = this;
        instance.openServer();
        try {
            start(creator);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected User createUser(String name) {
        return new TestUser(name);
    }

    @Override
    public boolean createUser(String name, StoredPassword password, boolean restricted) {
        User user = createUser(name);
        user.setPassword(password);
        user.setIsRestricted(restricted);
        return addUser(user);
    }

    @Override
    public String getConsoleFormat() {
        if(mConsoleFormat == null)
        {
            String date = "HH:mm:ss";
            mConsoleFormat = String.format("%%d{%s} [%%level]: %%msg%%n", date);
        }

        return mConsoleFormat;
    }
}
