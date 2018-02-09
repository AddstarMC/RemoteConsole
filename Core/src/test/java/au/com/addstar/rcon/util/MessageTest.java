package au.com.addstar.rcon.util;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;

import static org.junit.Assert.*;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 9/02/2018.
 */
public class MessageTest {
    private String message = "Test Message";
    private long time = 352011163283169L;
    private String thread = "TestThread";
    private String logger = "TestLogger";
    private String ServerId = "TestServerID";
    private String ServerName = "TestServer";
    private Message msg;


    @Before
    public void Setup(){
        msg = new Message(message, PacketOutMessage.MessageType.Directed,time, Level.WARNING,thread,logger,ServerId,ServerName);
    }

    @Test
    public void getMessage() {
        assertEquals(msg.getMessage(),message);
    }

    @Test
    public void getPlainMessage() {
        assertEquals(message, msg.getPlainMessage());
    }

    @Test
    public void setMessage() {
        msg.setMessage("New Test Message");
        assertEquals("New Test Message", msg.getMessage());
    }

    @Test
    public void getThreadName() {
        assertEquals(thread, msg.getThreadName());
    }

    @Test
    public void getTime() {
        assertEquals(time, msg.getTime());
    }

    @Test
    public void getType() {
        assertEquals(PacketOutMessage.MessageType.Directed, msg.getType());
    }

    @Test
    public void getLevel() {
        assertEquals(Level.WARNING, msg.getLevel());
    }

    @Test
    public void getLogger() {
        assertEquals(logger, msg.getLogger());
    }

    @Test
    public void getServerName() {
        assertEquals(ServerName,msg.getServerName());
    }

    @Test
    public void getServerId() {
        assertEquals(ServerId,msg.getServerId());

    }

    @Test
    public void setServer() {
        msg.setServer("NewID", "NewName");
        assertEquals("NewID", msg.getServerId());
        assertEquals("NewName", msg.getServerName());
    }

    @Test
    public void copyAs() {
        Message newMessage = msg.copyAs("New Message");
        assertNotEquals(msg,newMessage);
        assertEquals("New Message", newMessage.getMessage());
        assertEquals(msg.getType(),newMessage.getType());
    }

    @Test
    public void isRoughDuplicate() {
        Message newMessage = new Message(message, PacketOutMessage.MessageType.Directed,time+1, Level.WARNING,null,null,ServerId,ServerName);
        assertTrue(msg.isRoughDuplicate(newMessage));
    }

    @Test
    public void getFormatted() {
        msg = new Message(message, PacketOutMessage.MessageType.Directed,time, Level.WARNING,thread,logger,ServerId,ServerName);
        assertEquals("16:41:23 [TestServerID]: Test Message", msg.getFormatted("%d{HH:mm:ss} [%9sid]: %msg"));
    }

    @Test
    public void testOutString() {
        msg = new Message(message, PacketOutMessage.MessageType.Directed,time, Level.WARNING,thread,logger,ServerId,ServerName);
        assertEquals("Message: 352011163283169 [TestServerID] WARNING Test Message",msg.toString());
    }

}