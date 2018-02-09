package au.com.addstar.rcon.util;

import au.com.addstar.rcon.network.packets.login.PacketInEncryptGo;
import au.com.addstar.rcon.network.packets.login.PacketOutEncryptStart;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/02/2018.
 */
public class CryptHelperTest {

    private KeyPair serverkey;
    private SecretKey sharedKey;
    private byte[] mBlob = new byte[14];
    static  Random mRand = new Random();


    @Before
    public void setup(){
        CryptHelper.setDebug(true);
        serverkey = CryptHelper.generateKey();
        sharedKey = CryptHelper.generateSharedKey();

    }

    @Test
    public void CryptoTest() {
        mRand.nextBytes(mBlob);
        ByteBuf buffer  = Unpooled.buffer();
        PacketOutEncryptStart packet = new PacketOutEncryptStart(serverkey.getPublic(),mBlob);
        packet.write(buffer);
        PacketOutEncryptStart readPacket = new PacketOutEncryptStart();
        readPacket.read(buffer);
        assertNotNull(readPacket.key);
        assertEquals(readPacket.key,serverkey.getPublic());
        PacketInEncryptGo p2 = new PacketInEncryptGo(sharedKey,readPacket.key,mBlob);
        buffer.clear();
        p2.write(buffer);
        PacketInEncryptGo p3 = new PacketInEncryptGo();
        p3.read(buffer);
        assertNotNull(p3.secretKey);
        assertNotNull(p3.randomBlob);
        assertNotEquals(mBlob,p3.randomBlob);
        byte[] decodedBlob = CryptHelper.decrypt(serverkey.getPrivate(), p3.randomBlob);
        assertTrue(Arrays.equals(mBlob,decodedBlob));
        assertEquals(sharedKey, CryptHelper.decodeSecretKey(CryptHelper.decrypt(serverkey.getPrivate(), p3.secretKey)));

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
    public void encrypt(){
        PacketInEncryptGo p2 = new PacketInEncryptGo(sharedKey,null,mBlob);
        assertNull(p2.secretKey);
        assertNull(p2.randomBlob);
    }

    @Test
    public void decode() {
        byte[] blob = new byte[14];
        mRand.nextBytes(blob);
        PublicKey key = CryptHelper.decode(blob);
        assertNull(key);
    }

    @Test
    public void createContinuousCipher() {
        Cipher cipher = CryptHelper.createContinuousCipher(1,serverkey.getPrivate());
        assertNull(cipher);
        cipher = CryptHelper.createContinuousCipher(1,sharedKey);
        assertNotNull(cipher);
    }
}