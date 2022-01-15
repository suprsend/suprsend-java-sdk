package suprsend;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

public class Signature {
	private static final String HMAC_SHA256 = "HmacSHA256";
	public JSONObject getRequestSignature(String url, String httpVerb, JSONObject content, JSONObject headers, String secret) throws NoSuchAlgorithmException, MalformedURLException, InvalidKeyException {
		Mac sha256mac;
		String requestURI;
		String stringToSign;
		String signature;
		String contentTxt = "";
		String contentMD5 = "";
		
		JSONObject result = new JSONObject();
		
		if(httpVerb != "GET") {
			contentTxt = content.toString();
			contentMD5 = getMD5(contentTxt);
		}
		requestURI = getURI(url);
		stringToSign = String.format("%s\n%s\n%s\n%s\n%s", httpVerb, contentMD5, headers.get("Content-Type").toString(), headers.get("Date").toString(), requestURI);
		final byte[] byteKey = secret.getBytes(StandardCharsets.UTF_8);
		sha256mac = Mac.getInstance(HMAC_SHA256);
		SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
		sha256mac.init(keySpec);
		byte[] macData = sha256mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
		signature = Base64.getEncoder().encodeToString(macData);
		result.put("contentTxt", contentTxt);
		result.put("signature", signature);
		return result;
	}
	
	private String getMD5(String input) throws NoSuchAlgorithmException {
		String md5hex = DigestUtils.md5Hex(input);
		return md5hex;
	}
	
	private String getURI(String url) throws MalformedURLException {
		URL parsedURL = new URL(url);
		String requestURI = parsedURL.getPath();
		String query = parsedURL.getQuery();
		if(query != null) {
			requestURI = String.format("%s?%s", requestURI, query);
		}
		return requestURI;
	}
}
