package com.example.atlantic8.projectspartan;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.Encoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import Decoder.BASE64Encoder;
import Decoder.BASE64Decoder;

public class CipherPack {

	private static String AES_KEY_FILE = "AESKey";
	private static final String ALGORITHM = "RSA";
	private static final int KEYSIZE = 1024;
	// RSA最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;
	// RSA最大解密密文大小
	private static final int MAX_DECRYPT_BLOCK = 128;

	public SecretKey AESKey;
	public PublicKey publicKey;
	public PrivateKey privateKey;

	public CipherPack() {}

	public CipherPack(String aes_key, String public_key, String private_key) {
		this.StringToSecretKey(aes_key);
		this.StringToPublicKey(public_key);
		this.StringToPrivateKey(private_key);
	}


	public String getMD5(String data) {
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		try {
			byte[] btInput = data.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str).substring(8, 24);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * AES encryption
	 * @param content
	 * is the byte array to be encrypted
	 */
	public String AESEncryption(String content) {
		try {
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(byteContent);
			return parseByte2HexStr(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public String AESEncryption(String content, SecretKey AESKey) {
		try {
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] result = cipher.doFinal(byteContent);
			return parseByte2HexStr(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * AES decryption
	 * @param content
	 * is the byte array to be decrypted
	 */
	public String AESDecryption(String str) {
		try {
			byte[] content = parseHexStr2Byte(str);
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(content);
			return new String(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	public String AESDecryption(String str, SecretKey AESKey) {
		try {
			byte[] content = parseHexStr2Byte(str);
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] result = cipher.doFinal(content);
			return new String(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public String RSAEncrypt(String source) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] b = source.getBytes();
			byte[] b1 = cipher.doFinal(b);
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(b1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * use specific public key to encrypt
	 * @param source
	 * @param publicKey
	 * @return
	 */
	public String RSAEncrypt(String content, PublicKey publicKey) {
		byte[] data = content.getBytes();
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_ENCRYPT_BLOCK;
			}
			byte[] encryptedData = out.toByteArray();
			out.close();
			return new String(new BASE64Encoder().encode(encryptedData));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * @param cryptograph
	 * @return
	 * @throws Exception
	 */
	public String RSADecrypt(String cryptograph) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] b1 = decoder.decodeBuffer(cryptograph);
			byte[] b = cipher.doFinal(b1);
			return new String(b);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * use a specific private key to decrypt
	 * @param cryptograph
	 * @param privateKey
	 * @return
	 */
	public String RSADecrypt(String cryptograph, PrivateKey privateKey) {
		Cipher cipher;
		try {
            byte[] encryptedData = new BASE64Decoder().decodeBuffer(cryptograph);
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			int inputLen = encryptedData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段解密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
					cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_DECRYPT_BLOCK;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return new String(decryptedData);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param str
	 * @return get RSA private key from string
	 */
	public PrivateKey StringToPrivateKey (String pKey) {
		byte[] sikey;
		try {
			sikey = new Decoder.BASE64Decoder().decodeBuffer(pKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec pKCS8EncodedKeySpec =new PKCS8EncodedKeySpec(sikey);
			return keyFactory.generatePrivate(pKCS8EncodedKeySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void StringToPublicKey (String pKey) {
		byte[] gongkey;
		try {
			gongkey = new Decoder.BASE64Decoder().decodeBuffer(pKey);
			KeyFactory keyFactory2 = KeyFactory.getInstance("RSA"); 
			X509EncodedKeySpec x509KeySpec2 = new X509EncodedKeySpec(gongkey);
			publicKey = keyFactory2.generatePublic(x509KeySpec2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PublicKey StringToPublicKey2 (String pKey) {
		byte[] gongkey;
		try {
			gongkey = new Decoder.BASE64Decoder().decodeBuffer(pKey);
			KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec x509KeySpec2 = new X509EncodedKeySpec(gongkey);
			return keyFactory2.generatePublic(x509KeySpec2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SecretKey OriginalStringToSecretKey (String pKey) {
		try {
			// encode to BASE64 first\
			String tmpKey = Base64.encodeToString(pKey.getBytes() , Base64.DEFAULT);
			Log.e("AES tmp key", tmpKey);

			byte[] decodedKey = Base64.decode(tmpKey, Base64.DEFAULT);
			// byte[] decodedKey = Base64.getEncoder().encode(pKey.getBytes());
			// System.out.println(decodedKey.length);
			return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SecretKey StringToSecretKey (String pKey) {
		try {
			byte[] decodedKey = Base64.decode(pKey, Base64.DEFAULT);
			// byte[] decodedKey = Base64.getEncoder().encode(pKey.getBytes());
			// System.out.println(decodedKey.length);
			return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param key
	 * @return transfer key to string
	 */
	public String SecretKeyToString(SecretKey key) {
		//String s = new Decoder.BASE64Encoder().encodeBuffer(key.getEncoded());
		//System.out.println(s);
		String tmp = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
		return tmp;
		// return new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
	}
	
	public String KeyToString(PrivateKey key) {
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}
	
	public String KeyToString(PublicKey key) {
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}

}
