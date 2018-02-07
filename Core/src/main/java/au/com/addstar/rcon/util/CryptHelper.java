package au.com.addstar.rcon.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class CryptHelper
{
	public static void setDebug(boolean debug) {
		CryptHelper.debug = debug;
	}

	private static boolean debug;


	public static PublicKey decode(byte[] data)
	{
		try
        {
            X509EncodedKeySpec x509encodedkeyspec = new X509EncodedKeySpec(data);
            KeyFactory keyfactory = KeyFactory.getInstance("RSA");
            return keyfactory.generatePublic(x509encodedkeyspec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			if(debug){
				System.out.println("data: "+ Arrays.toString(data));
				e.printStackTrace();
			}
			System.err.println("Public key reconstitute failed!");
			return null;
		}
	}
	
	public static byte[] encrypt(Key key, byte[] data)
	{
		return cipherOp(Cipher.ENCRYPT_MODE, key, data);
	}
	
	public static byte[] decrypt(Key key, byte[] data)
	{
		return cipherOp(Cipher.DECRYPT_MODE, key, data);
	}
	
	private static byte[] cipherOp(int op, Key key, byte[] data)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
	        cipher.init(op, key);
	        return cipher.doFinal(data);
		}
		catch(Exception e)
		{
			if(debug){
				System.out.println("Op: " +op);
				System.out.println("Key: " +key.toString());
				System.out.println("data: "+ Arrays.toString(data));
			}
                e.printStackTrace();
                return null;

		}
	}
	
	public static Cipher createContinuousCipher(int op, Key key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
			cipher.init(op, key, new IvParameterSpec(key.getEncoded()));
			return cipher;
		}
		catch(GeneralSecurityException e)
		{
			if(debug){
				System.out.println("Op: " +op);
				System.out.println("Key: " +key.toString());
			}
			e.printStackTrace();
			return null;
		}
	}

	public static KeyPair generateKey()
	{
		try
		{
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			return gen.generateKeyPair();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static SecretKey generateSharedKey()
	{
		try
		{
			KeyGenerator gen = KeyGenerator.getInstance("AES");
			return gen.generateKey();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static SecretKey decodeSecretKey( byte[] data )
	{
		return new SecretKeySpec(data, "AES");
	}
}
