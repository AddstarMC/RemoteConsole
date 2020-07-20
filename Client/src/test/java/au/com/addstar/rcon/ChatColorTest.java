package au.com.addstar.rcon;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 19/07/2020.
 */
public class ChatColorTest {

    @Test
    public void translateColors() {
        String test = "§x§0§0§0§0§0§0Black§x§f§f§f§f§f§fWhite";
        String result = ChatColor.translateColors(test);
        System.out.println(result);
        assertEquals("\u001B[38;2;0;0;0mBlack\u001B[38;2;255;255;255mWhite",result);
    }
}