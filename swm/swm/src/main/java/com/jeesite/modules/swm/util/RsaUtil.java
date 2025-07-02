package com.jeesite.modules.swm.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
public class RsaUtil {

	public static final String CHAR_ENCODING = "UTF-8";
	public static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";
	public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDogXyg7peohiA5R+18DctstTQp0Uc3fQJOUhjXsIEVCFy7L4dEG3bWGZ2A2AVSUL9t8CHeElIKtkpP1PZjU5EVh3R82srkkSbbheyl9f4IUC1wEVNwtr30OFpKZB0EZYUUNIu0cyyV4MNBPR8zxWRZtoAE27g97M51uYthETVQGp0aOBVSnEA56/mjXaT2nsY86aPCCtL8dDWHMsJiHqc3U25VweMpEbggLOG1fHhYcqtLLReeJpvBJWrNhGeu0/xe/KnAwdvwooJeXn9Avy9fAHjSCHys8NsCd6cm6cgHp+W1W+BI1ETgd44xtesjHJs5ZNdSNn+kBgMLW7zhA2a/AgMBAAECggEBANo4HgbVqJXNPRVGAwk50UG4WLse5t6Xf8COoS5fROe+r3ooQ6aSuFh+NiullGkLzH+cr8zEGShgqyJ+WLokxrkUJyrAF0mrSnMjIVcqqECZL+xM9qmFeodrFKNZjzp/JgYuwyicKK6LA/eJLG3kyCjDdygYKLZGOgiYGs8B3a3NYrN46fEKcUd9mlq98mL7mHkJY4srtKSEmtD/PWdO6HuanmS4lPlgqSfbdbyLOcUQpfAAGrxKswz61KAe7JRYFjWRNQpSrO5+OeTKf+Wa3QFsvc0dHH6uAMWtJAimbOld5UqrEdhUwpIzqA8Zfp6T5fqMf2DpHh7ruf0urb+yNnECgYEA+OhvvOcosnazaZAOxmUkpP/bQC06SZslWPFHiGu3cqwIesweuxVbifPAAHsHHnA22fnSUvi97eg3mGGPFEoTFVS5uZCTv1pyT2LGNuDOpBTgnVXBwIbIihU8i1rOvbAK7g+PD6YpPiofzpCyTQ72SmdR17tEBHqbzLI0OQOZ6/UCgYEA7yFpQ7XOhhaY51das4NxcSc06Ms7EOh5Tsg8fZEItJiZGHtMsq+YCZispodO+hkzfVgWoETt9HoBGgT+gQlJzXQ5/NFDstGoSSUc0g0Bha+31ECsep/kR30gKl1pxOnzjsGokiA0su3A8bxCjAYjczI817qrzIs8xuV93wSPK2MCgYEAlCe+QsGQ/tEar43YzYxsiG3mskd5d1CKpWtQecor8myly3nuHMt0piNZOLACJ8MBUzOZVlvKqW5ckS4YvSnuO/cnaWW+G9sZSVlwxD+BoDbxD5V480EG4vILDKOrhUrg5pyKOfVcfS0Tq5+DEc0DGnxvQaqsrMHSNMApx3n2R3UCgYEA3OmA1YyfvkAiAZYxtKU5p88Qkf8uEfCyIJXbTCUwZaaTyIof0PNnAXaKCU33KBUf/CvkuyryqWgMnH0AoTRzedplt6mbYRdO4EPo3CQnw9kReoRE0wPjYCe4D2s8yy1WVAuUL2qRRLtgA/Jnh/Qjy8gdbS6Gh/AZtaVNIzqTCQECgYAkJbldPcIX5RRJB8MZKfWtL2Hwk5ifmBq/qMFUrlwZ4uL4oWTCgBSyx76bxminWiO5HZK8JYYG73Gw1FT5FSUgOcAh0fwsXGXkO3z0EPhuXDGbsBFNTXm91qC5Kln0OhmZwgx6wCy/m2J6+/UhNtBvPsKjHpd1Lg0YNfA9R61XVw==";
	public static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6IF8oO6XqIYgOUftfA3LbLU0KdFHN30CTlIY17CBFQhcuy+HRBt21hmdgNgFUlC/bfAh3hJSCrZKT9T2Y1ORFYd0fNrK5JEm24XspfX+CFAtcBFTcLa99DhaSmQdBGWFFDSLtHMsleDDQT0fM8VkWbaABNu4PezOdbmLYRE1UBqdGjgVUpxAOev5o12k9p7GPOmjwgrS/HQ1hzLCYh6nN1NuVcHjKRG4ICzhtXx4WHKrSy0XniabwSVqzYRnrtP8XvypwMHb8KKCXl5/QL8vXwB40gh8rPDbAnenJunIB6fltVvgSNRE4HeOMbXrIxybOWTXUjZ/pAYDC1u84QNmvwIDAQAB";

	/**
	 * 加密方法 source： 源数据
	 */
	public static String encrypt(String source, String publicKey) throws Exception {
		Key key = getPublicKey(publicKey);
		/** 得到Cipher对象来实现对源数据的RSA加密 */
		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] b = source.getBytes();
		/** 执行加密操作 */
		byte[] b1 = cipher.doFinal(b);
		return new String(Base64.encodeBase64(b1),
				CHAR_ENCODING);
	}

	/**
	 * 解密算法 cryptograph:密文
	 */
	public static String decrypt(String cryptograph, String privateKey) throws Exception {
		Key key = getPrivateKey(privateKey);
		/** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
		Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] b1 = Base64.decodeBase64(cryptograph.getBytes());
		/** 执行解密操作 */
		byte[] b = cipher.doFinal(b1);
		return new String(b);
	}

	/**
	 * 得到公钥
	 * 
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String key) throws Exception {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
				Base64.decodeBase64(key.getBytes()));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 得到私钥
	 * 
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String key) throws Exception {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
				Base64.decodeBase64(key.getBytes()));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	public static String sign(String content, String privateKey) {
		String charset = CHAR_ENCODING;
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decodeBase64(privateKey.getBytes()));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			Signature signature = Signature.getInstance("SHA1WithRSA");

			signature.initSign(priKey);
			signature.update(content.getBytes(charset));

			byte[] signed = signature.sign();

			return new String(Base64.encodeBase64(signed));
		} catch (Exception e) {

		}

		return null;
	}

	public static boolean checkSign(String content, String sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decodeBase64(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			Signature signature = Signature
					.getInstance("SHA1WithRSA");

			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));

			boolean bverify = signature.verify(Base64.decodeBase64(sign));
			return bverify;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 字符串加密
	 * 
	 * @param requestURI
	 * @return
	 * @throws Exception
	 */
	public static String getEncrypt(String requestURI) throws Exception {
		String encrypt = RsaUtil.encrypt(requestURI, publicKey);
		return encrypt;
	}

	public static void main(String[] args) throws Exception {
		String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDogXyg7peohiA5R+18DctstTQp0Uc3fQJOUhjXsIEVCFy7L4dEG3bWGZ2A2AVSUL9t8CHeElIKtkpP1PZjU5EVh3R82srkkSbbheyl9f4IUC1wEVNwtr30OFpKZB0EZYUUNIu0cyyV4MNBPR8zxWRZtoAE27g97M51uYthETVQGp0aOBVSnEA56/mjXaT2nsY86aPCCtL8dDWHMsJiHqc3U25VweMpEbggLOG1fHhYcqtLLReeJpvBJWrNhGeu0/xe/KnAwdvwooJeXn9Avy9fAHjSCHys8NsCd6cm6cgHp+W1W+BI1ETgd44xtesjHJs5ZNdSNn+kBgMLW7zhA2a/AgMBAAECggEBANo4HgbVqJXNPRVGAwk50UG4WLse5t6Xf8COoS5fROe+r3ooQ6aSuFh+NiullGkLzH+cr8zEGShgqyJ+WLokxrkUJyrAF0mrSnMjIVcqqECZL+xM9qmFeodrFKNZjzp/JgYuwyicKK6LA/eJLG3kyCjDdygYKLZGOgiYGs8B3a3NYrN46fEKcUd9mlq98mL7mHkJY4srtKSEmtD/PWdO6HuanmS4lPlgqSfbdbyLOcUQpfAAGrxKswz61KAe7JRYFjWRNQpSrO5+OeTKf+Wa3QFsvc0dHH6uAMWtJAimbOld5UqrEdhUwpIzqA8Zfp6T5fqMf2DpHh7ruf0urb+yNnECgYEA+OhvvOcosnazaZAOxmUkpP/bQC06SZslWPFHiGu3cqwIesweuxVbifPAAHsHHnA22fnSUvi97eg3mGGPFEoTFVS5uZCTv1pyT2LGNuDOpBTgnVXBwIbIihU8i1rOvbAK7g+PD6YpPiofzpCyTQ72SmdR17tEBHqbzLI0OQOZ6/UCgYEA7yFpQ7XOhhaY51das4NxcSc06Ms7EOh5Tsg8fZEItJiZGHtMsq+YCZispodO+hkzfVgWoETt9HoBGgT+gQlJzXQ5/NFDstGoSSUc0g0Bha+31ECsep/kR30gKl1pxOnzjsGokiA0su3A8bxCjAYjczI817qrzIs8xuV93wSPK2MCgYEAlCe+QsGQ/tEar43YzYxsiG3mskd5d1CKpWtQecor8myly3nuHMt0piNZOLACJ8MBUzOZVlvKqW5ckS4YvSnuO/cnaWW+G9sZSVlwxD+BoDbxD5V480EG4vILDKOrhUrg5pyKOfVcfS0Tq5+DEc0DGnxvQaqsrMHSNMApx3n2R3UCgYEA3OmA1YyfvkAiAZYxtKU5p88Qkf8uEfCyIJXbTCUwZaaTyIof0PNnAXaKCU33KBUf/CvkuyryqWgMnH0AoTRzedplt6mbYRdO4EPo3CQnw9kReoRE0wPjYCe4D2s8yy1WVAuUL2qRRLtgA/Jnh/Qjy8gdbS6Gh/AZtaVNIzqTCQECgYAkJbldPcIX5RRJB8MZKfWtL2Hwk5ifmBq/qMFUrlwZ4uL4oWTCgBSyx76bxminWiO5HZK8JYYG73Gw1FT5FSUgOcAh0fwsXGXkO3z0EPhuXDGbsBFNTXm91qC5Kln0OhmZwgx6wCy/m2J6+/UhNtBvPsKjHpd1Lg0YNfA9R61XVw==";
		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6IF8oO6XqIYgOUftfA3LbLU0KdFHN30CTlIY17CBFQhcuy+HRBt21hmdgNgFUlC/bfAh3hJSCrZKT9T2Y1ORFYd0fNrK5JEm24XspfX+CFAtcBFTcLa99DhaSmQdBGWFFDSLtHMsleDDQT0fM8VkWbaABNu4PezOdbmLYRE1UBqdGjgVUpxAOev5o12k9p7GPOmjwgrS/HQ1hzLCYh6nN1NuVcHjKRG4ICzhtXx4WHKrSy0XniabwSVqzYRnrtP8XvypwMHb8KKCXl5/QL8vXwB40gh8rPDbAnenJunIB6fltVvgSNRE4HeOMbXrIxybOWTXUjZ/pAYDC1u84QNmvwIDAQAB";
		String uri = "/hls/000006/index.m3u8";

		String xxx = "CZiSXnxOtUbD8A+TU2aINN++FvCDO9Za/Q7z2tJNHwkkYtr5hN2UnoOFHSEXLkCBlSozI0nF2vFw6Ephr9lFJpQEUsgbmfrrVxS8yZv4AIeCNfDQoBSWJC6ArxAktm3DZnMm//lyJgCn3lO6rQAvG3ydcN+Wi6Md2+8sRJ166mZoXz2L78ew7n7KshJ6wqGo6aGHlM6I7OuoVqUfCwRAPrNwXhhxhr9a0fK8RQGTRAj3K6rJV5g31VaHPadTuHQGx0L6/7BpsxtFsZ7IVXhv8nHU0ne9XZsx+pxa2TyS4Wke+/vE4YCU0HIWvdVgKh5ln06A8DeTLMgkP23tRZ9/2w==";

		String www = URLEncoder.encode(xxx, StandardCharsets.UTF_8.displayName());
		log.info("解密结果：{}", www);

		String encrypt = RsaUtil.encrypt(uri, publicKey);
		log.info("加密结果：{}", encrypt);
		String decrypt = RsaUtil.decrypt(encrypt, privateKey);
		log.info("解密结果：{}", decrypt);
	}
}
