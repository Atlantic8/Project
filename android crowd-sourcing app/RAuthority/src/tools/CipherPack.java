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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CipherPack {
	public SecretKey AESKey;
	private static String AES_KEY_FILE = "AESKey";
	/** 指定加密算法为RSA */
	private static final String ALGORITHM = "RSA";
	/** 密钥长度，用来初始化 */
	private static final int KEYSIZE = 1024;
	/** 指定公钥存放文件 */
	private static String PUBLIC_KEY_FILE = "PublicKey";
	/** 指定私钥存放文件 */
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
		} else if (type.equals("NONE") == true) {
			// System.out.println("Will not initialize any cipher object.");
		} else {
			System.out.println("Exception: For now, this algorithm is not implemented yet.");
		}
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
				// System.out.println("AES key initialized.");
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
				// System.out.println("AES key initialized.");
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// generate aes key and set it to be AESKey.
	public void GenerateAESKey() {
		KeyGenerator keygen;
		try {
			keygen = KeyGenerator.getInstance("AES");
			SecureRandom random = new SecureRandom();
			keygen.init(random);
			//KeyGenerator kgen = KeyGenerator.getInstance("AES");   
            //kgen.init(128, new SecureRandom(password.getBytes()));   
            //SecretKey secretKey = kgen.generateKey(); 
			AESKey = keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println("Fail to generate AES key.");
			e.printStackTrace();
		}
	}
	
	/**
	 * AES encryptiopn
	 * 
	 * @param content
	 *            is the byte array to be encrypted
	 */
	public String AESEncryption(String content, SecretKey AESKey1) {
		try {
			byte[] enCodeFormat = AESKey1.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return parseByte2HexStr(result); // 加密
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
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
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
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
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
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

		// RSA算法要求有一个可信任的随机数源 */
		// SecureRandom secureRandom = new SecureRandom();
		/** 为RSA算法创建一个KeyPairGenerator对象 */
		File f1 = new File(this.PUBLIC_KEY_FILE), f2 = new File(this.PRIVATE_KEY_FILE);
		if (f1.exists() & f2.exists() == true) {
			ObjectInputStream ois1 = null, ois2 = null;
			try {
				f1.createNewFile();
				f2.createNewFile();
				/** 将文件中的公钥对象读出 */
				ois1 = new ObjectInputStream(new FileInputStream(this.PUBLIC_KEY_FILE));
				publicKey = (PublicKey) ois1.readObject();
				ois1.close();
				/** 将文件中的私钥对象读出 */
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
				/** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
				// keyPairGenerator.initialize(KEYSIZE, secureRandom);
				keyPairGenerator.initialize(KEYSIZE);
				/** 生成密匙对 */
				KeyPair keyPair = keyPairGenerator.generateKeyPair();
				/** 得到公钥 */
				publicKey = keyPair.getPublic();
				/** 得到私钥 */
				privateKey = keyPair.getPrivate();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			}

			ObjectOutputStream oos1 = null;
			ObjectOutputStream oos2 = null;
			try {
				/** 用对象流将生成的密钥写入文件 */
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
		// System.out.println("RSA keys initialized.");
	}

	
	// generate rsa keys and set them to be publicKey and privateKey.
	public void generateRSAKeyPair() {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
			/** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
			// keyPairGenerator.initialize(KEYSIZE, secureRandom);
			keyPairGenerator.initialize(KEYSIZE);
			/** 生成密匙对 */
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			/** 得到公钥 */
			publicKey = keyPair.getPublic();
			/** 得到私钥 */
			privateKey = keyPair.getPrivate();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	
	/**
	 * 加密方法
	 * 
	 * @param source
	 *            源数据
	 * @return
	 * @throws Exception
	 */
	public String RSAEncrypt(String source, PublicKey publicKey) {
		ObjectInputStream ois = null;
		try {
			/** 得到Cipher对象来实现对源数据的RSA加密 */
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] b = source.getBytes();
			/** 执行加密操作 */
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
	 * 解密算法
	 * 
	 * @param cryptograph
	 *            密文
	 * @return
	 * @throws Exception
	 */
	public String RSADecrypt(String cryptograph) {
		/** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] b1 = decoder.decodeBuffer(cryptograph);
			/** 执行解密操作 */
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
		/** 得到Cipher对象,对已用公钥加密的数据进行RSA解密 */
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] b1 = decoder.decodeBuffer(cryptograph);
			/** 执行解密操作 */
			byte[] b = cipher.doFinal(b1);
			return new String(b);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/**
	 * 将16进制转换为二进制
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
	 * 将二进制转换成16进制
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
	
	public SecretKey OriginalStringToSecretKey (String pKey) {
		try {
			// encode to BASE64b first
			byte[] tmpKey = Base64.getEncoder().encode(pKey.getBytes());
			System.out.println("tmp key is: "+new String(tmpKey));
			
			byte[] decodedKey = Base64.getDecoder().decode(new String(tmpKey));
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
			byte[] decodedKey = Base64.getDecoder().decode(pKey.getBytes());
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
		String s = Base64.getEncoder().encodeToString(key.getEncoded());
		//String s = Base64.getDecoder().decode(key.getEncoded()).toString();
		//String s = new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
		// System.out.println(s);
		return s;
	}
	
	public String KeyToString(PrivateKey key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public String KeyToString(PublicKey key) {
		// return new sun.misc.BASE64Encoder().encodeBuffer(key.getEncoded());
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

}
