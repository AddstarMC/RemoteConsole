package au.com.addstar.rcon.util;

import org.junit.Before;
import org.junit.Test;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/02/2018.
 */
public class CryptHelperTest {

    private KeyPair serverkey;
    private Key sharedKey;
    @Before
    public void setup(){
        serverkey = CryptHelper.generateKey();
    }

    @Test
    public void decode() {
        CryptHelper.setDebug(true);
        String test = "Test String";
        byte[] data = test.getBytes();
       // CryptHelper.decode(data);
    }

    @Test
    public void encrypt() {
    }

    @Test
    public void decrypt() {
    }

    @Test
    public void generateKey() {
        assertNotNull(serverkey);
        PublicKey pub = serverkey.getPublic();
        assertNotNull(pub);
        assertEquals("RSA",pub.getAlgorithm());
        PrivateKey priv = serverkey.getPrivate();
        assertNotNull(priv);
        assertEquals("RSA",priv.getAlgorithm());

    }

    @Test
    public void generateSharedKey() {
        sharedKey = CryptHelper.generateSharedKey();
        assertNotNull(sharedKey);
        assertEquals("AES",sharedKey.getAlgorithm());
    }

    @Test
    public void decodeSecretKey() {

    }
}