package au.com.addstar.rcon;

import au.com.addstar.rcon.server.RconServer;
import au.com.addstar.rcon.server.ServerNetworkManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;

public class RemoteConsoleLogAppender extends AbstractAppender {
    protected RemoteConsoleLogAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        // Ignore log messages from ourselves
        if (event.getThreadName().startsWith("Log4j2-TF-1-AsyncLogger") || RconServer.instance == null)
            return;

        /*System.out.println("RemoteConsoleLogAppender: "
                + event.getLoggerName() + "("
                + event.getThreadName() + "/"
                + event.getThreadId() + ") -> "
                + event.getLevel().toString() + ": "
                + event.getMessage().getFormattedMessage());*/

        for (ServerNetworkManager connection : RconServer.instance.getConnections()) {
            VelocityUser user = (VelocityUser) connection.getUser();
            if (user != null) {
                try {
                    user.asCommandSender().sendRichMessage(event.getMessage().getFormattedMessage());
                } catch (Exception e) {
                    System.out.println("Error sending message to " + user.getName() + ": " + e.getMessage());
                }
            } else {
                System.out.println("RconUser is null for connection");
            }
        }
    }
}
