package tools;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CipherPack {
	public SecretKey AESKey;
	private static String AES_KEY_FILE = "AESKey";
	private static final String AESTYPE = "AES/ECB/PKCS5Padding";
	/** ָ�������㷨ΪRSA */
	private static final String ALGORITHM = "RSA";
	/** ��Կ���ȣ�������ʼ�� */
	private static final int KEYSIZE = 1024;
	/** ָ����Կ����ļ� */
	private static String PUBLIC_KEY_FILE = "PublicKey";
	/** ָ��˽Կ����ļ� */
	private static String PRIVATE_KEY_FILE = "PrivateKey";
	public PublicKey publicKey;
	public PrivateKey privateKey;

	public CipherPack() {
		this.initAESKey();
		this.initRSAKeyPair();
	}

	public CipherPack(String type) {
		if (type.equals("AES") == true) {
			this.initAESKey();
		} else if (type.equals("RSA") == true) {
			this.initRSAKeyPair();
		} else {
			System.out.println("Exception: For now, this algorithm is not implemented yet.");
		}
	}

	/**
	 * initiate AES secret key
	 * 
	 */
	private void initAESKey() {
		File file = new File(AES_KEY_FILE);
		if (file.exists() == false) {
			ObjectOutputStream os = null;
			try {
				file.createNewFile();
				KeyGenerator keygen = KeyGenerator.getInstance("AES");
				SecureRandom random = new SecureRandom();
				keygen.init(random);
				AESKey = keygen.generateKey();
				os = new ObjectOutputStream(new FileOutputStream(AES_KEY_FILE));
				os.writeObject(AESKey);
				os.flush();
				os.close();
				System.out.println("AES key initialized.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			ObjectInputStream is = null;
			try {
				is = new ObjectInputStream(new FileInputStream(file));
				AESKey = (SecretKey) is.readObject();
				is.close();
				System.out.println("AES key initialized.");
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * AES encryptiopn
	 * 
	 * @param content
	 *            is the byte array to be encrypted
	 */
	public String AESEncryption(String content) {
		try {
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// ����������
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// ��ʼ��
			byte[] result = cipher.doFinal(byteContent);
			return parseByte2HexStr(result); // ����
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * AES decryption
	 * 
	 * 
	 * @param content
	 *            is the byte array to be decrypted
	 */
	public String AESDecryption(String str) {
		try {
			byte[] content = parseHexStr2Byte(str);
			byte[] enCodeFormat = AESKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// ����������
			cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��
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
			Cipher cipher = Cipher.getInstance("AES");// ����������
			cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��
			byte[] result = cipher.doFinal(content);
			return new String(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * generate RSA key pairs
	 * 
	 * store key pairs in file
	 * 
	 */
	private void initRSAKeyPair() {

		// RSA�㷨Ҫ����һ�������ε������Դ */
		// SecureRandom secureRandom = new SecureRandom();
		/** ΪRSA�㷨����һ��KeyPairGenerator���� */
		File f1 = new File(this.PUBLIC_KEY_FILE), f2 = new File(this.PRIVATE_KEY_FILE);
		if (f1.exists() & f2.exists() == true) {
			ObjectInputStream ois1 = null, ois2 = null;
			try {
				f1.createNewFile();
				f2.createNewFile();
				/** ���ļ��еĹ�Կ������� */
				ois1 = new ObjectInputStream(new FileInputStream(this.PUBLIC_KEY_FILE));
				publicKey = (PublicKey) ois1.readObject();
				ois1.close();
				/** ���ļ��е�˽Կ������� */
				ois2 = new ObjectInputStream(new FileInputStream(this.PRIVATE_KEY_FILE));
				privateKey = (PrivateKey) ois2.readObject();
				ois2.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		} else {
			KeyPairGenerator keyPairGenerator;
			try {
				keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
				/** ����������������Դ��ʼ�����KeyPairGenerator���� */
				// keyPairGenerator.initialize(KEYSIZE, secureRandom);
				keyPairGenerator.initialize(KEYSIZE);
				/** �����ܳ׶� */
				KeyPair keyPair = keyPairGenerator.generateKeyPair();
				/** �õ���Կ */
				publicKey = keyPair.getPublic();
				/** �õ�˽Կ */
				privateKey = keyPair.getPrivate();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			}

			ObjectOutputStream oos1 = null;
			ObjectOutputStream oos2 = null;
			try {
				/** �ö����������ɵ���Կд���ļ� */
				oos1 = new ObjectOutputStream(new FileOutputStream(PUBLIC_KEY_FILE));
				oos2 = new ObjectOutputStream(new FileOutputStream(PRIVATE_KEY_FILE));
				oos1.writeObject(publicKey);
				oos2.writeObject(privateKey);
				oos1.close();
				oos2.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("RSA keys initialized.");
	}

	/**
	 * ���ܷ���
	 * 
	 * @param source
	 *            Դ����
	 * @return
	 * @throws Exception
	 */
	public String RSAEncrypt(String source) {
		ObjectInputStream ois = null;
		try {
			/** �õ�Cipher������ʵ�ֶ�Դ���ݵ�RSA���� */
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] b = source.getBytes();
			/** ִ�м��ܲ��� */
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
	 * �����㷨
	 * 
	 * @param cryptograph
	 *            ����
	 * @return
	 * @throws Exception
	 */
	public String RSADecrypt(String cryptograph) {
		/** �õ�Cipher��������ù�Կ���ܵ����ݽ���RSA���� */
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] b1 = decoder.decodeBuffer(cryptograph);
			/** ִ�н��ܲ��� */
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
		/** �õ�Cipher��������ù�Կ���ܵ����ݽ���RSA���� */
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] b1 = decoder.decodeBuffer(cryptograph);
			/** ִ�н��ܲ��� */
			byte[] b = cipher.doFinal(b1);
			return new String(b);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/**
	 * ��16����ת��Ϊ������
	 * 
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
	 * ��������ת����16����
	 * 
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
			sikey = new sun.misc.BASE64Decoder().decodeBuffer(pKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec pKCS8EncodedKeySpec =new PKCS8EncodedKeySpec(sikey);
			return keyFactory.generatePrivate(pKCS8EncodedKeySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public PublicKey StringToPublicKey (String pKey) {
		byte[] gongkey;
		try {
			gongkey = new sun.misc.BASE64Decoder().decodeBuffer(pKey);
			KeyFactory keyFactory2 = KeyFactory.getInstance("RSA"); 
			X509EncodedKeySpec x509KeySpec2 = new X509EncodedKeySpec(gongkey);
			return keyFactory2.generatePublic(x509KeySpec2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public SecretKey StringToSecretKey (String pKey) {
		try {
			byte[] encodedKey = new sun.misc.BASE64Decoder().decodeBuffer(pKey);
			return new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");
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
		String s = new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
		System.out.println(s);
		return s;
		// return new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
	}
	
	public String KeyToString(PrivateKey key) {
		return new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
	}
	
	public String KeyToString(PublicKey key) {
		return new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
	}

}
