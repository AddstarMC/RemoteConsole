package au.com.addstar.rcon;

import au.com.addstar.rcon.server.User;
import au.com.addstar.rcon.server.auth.IUserStore;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/02/2018.
 */
public class UserStore implements IUserStore {

    private HashMap<String, User> users;

    UserStore() {
        this.users = new HashMap<>();
    }

    @Override
    public void initialize() throws IOException {

    }

    @Override
    public void shutdown() throws IOException {

    }

    @Override
    public boolean loadUser(User user) throws IOException {
        User mapped = users.get(user.getName());
        user.setPassword(mapped.getPassword());
        user.setIsRestricted(mapped.isRestricted());
        return true;
    }

    @Override
    public void saveUser(User user) throws IOException {
        users.put(user.getName(),user);
    }

    @Override
    public void addUser(User user) throws IOException {
        users.put(user.getName(),user);

    }

    @Override
    public void removeUser(User user) throws IOException {
        users.remove(user.getName());

    }
}
