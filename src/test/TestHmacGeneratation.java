package test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TestHmacGeneratation {
	public static void main(String[] args) throws Exception {
		String distinct_id = "b8278572-2929-4af6-be2b-cdc2bc1f6256";
		String secret = "IG-J8Wvf7M-w4ll13h53NJAMQQNHdUqFTSJ2JVAZl0s";
		TestHmacGeneratation instance = new TestHmacGeneratation();
		String output = instance.hmacRawURLSafeBase64String(distinct_id, secret);
		System.out.println(output);
		// prints dHBWYF4oV190o4j-e3eYxB-SCkeHnoaiofe8EmGk9JQ
	}
	
	public String hmacRawURLSafeBase64String(String distinctId, String secret) throws InvalidKeyException, NoSuchAlgorithmException {
		Mac sha256mac = getSha256macInstance(secret);
		byte[] macData = sha256mac.doFinal(distinctId.getBytes(StandardCharsets.UTF_8));
		String hmacString = Base64.getUrlEncoder().withoutPadding().encodeToString(macData);
		return hmacString;
	}

	private Mac getSha256macInstance(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
		final byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
		Mac sha256mac;
		try {
			sha256mac = Mac.getInstance("HmacSHA256");
			sha256mac.init(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (InvalidKeyException e) {
			throw e;
		}
		return sha256mac;
	}
}
