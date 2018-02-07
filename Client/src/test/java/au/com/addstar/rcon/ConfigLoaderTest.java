package au.com.addstar.rcon;

import au.com.addstar.rcon.objects.TestConsole;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 5/02/2018.
 */
public class ConfigLoaderTest {

    @Before
    public void setup() {
        ConsoleScreen screen = new TestConsole();
        ClientMain.mInstance = new ClientMain(screen, "testUser", "testPassword", true);
    }


    @Test
    public void testloadConfig() {
        try {
            ConfigLoader.loadConfig(this.getClass().getResourceAsStream("/test.xml"));
        }catch (IOException e){
            e.printStackTrace();
        }
        assertNotNull(ClientMain.getViewManager().getView("chat"));
        assertNotNull(ClientMain.getViewManager().getView("log"));
    }
    @Test
    public void testLoaderException(){
        try {
            ConfigLoader.loadConfig(this.getClass().getResourceAsStream("/test2.xml"));
        }catch (IOException e){
            assertEquals("java.lang.IllegalArgumentException: InputStream cannot be null",e.getMessage());
        }
    }
    @Test
    public void testBadXml(){
        try {
            ConfigLoader.loadConfig(this.getClass().getResourceAsStream("/fault.xml"));
        }catch (IOException e){
            assertEquals("org.xml.sax.SAXParseException; lineNumber: 25; columnNumber: 17; Element type \"filters\" must be followed by either attribute specifications, \">\" or \"/>\".",e.getMessage());
        }
    }
}