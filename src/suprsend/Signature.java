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

/**
 * This class creates the signature which needs to be passed to 
 * workflow backend. It will be validated, if true workflow will be triggered.
 * MD5 hash and HMAC 256 algorithm will be used for the same. 
 * @author Suprsend
 *
 */
public class Signature {
	private static final String HMAC_SHA256 = "HmacSHA256";
	
	/**
	 * Get request signature
	 * @param url
	 * 		  Workflow backend URL
	 * @param httpVerb
	 *        HTTP method
	 * @param content
	 *        Raw content
	 * @param headers
	 *        Raw headers
	 * @param secret
	 *        Environment secret key given to client by Suprsend
	 * @return
	 *        JSON object which contains raw content in string format and signature.
	 * @throws SuprsendException
	 */
	public JSONObject getRequestSignature(String url, String httpVerb, JSONObject content, JSONObject headers, String secret) throws SuprsendException {
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
		try {
			sha256mac = Mac.getInstance(HMAC_SHA256);
		} catch (NoSuchAlgorithmException e) {
			throw new SuprsendException("Invalid algorithm name passed.", e);
		}
		SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
		try {
			sha256mac.init(keySpec);
		} catch (InvalidKeyException e) {
			throw new SuprsendException("Invalid key passed.", e);
		}
		byte[] macData = sha256mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
		signature = Base64.getEncoder().encodeToString(macData);
		result.put("contentTxt", contentTxt);
		result.put("signature", signature);
		return result;
	}
	
	/**
	 * Get MD5 hash digest for body
	 * @param input
	 * 		  String format of JSON body.
	 * @return
	 * 		  MD5 hash digest of passed input.
	 */
	private String getMD5(String input) {
		String md5hex = DigestUtils.md5Hex(input);
		return md5hex;
	}
	
	/**
	 * URI with passed query string
	 * @param url
	 * 	      Passed URL
	 * @return
	 * 	      URI
	 * @throws SuprsendException
	 */
	private String getURI(String url) throws SuprsendException {
		URL parsedURL;
		try {
			parsedURL = new URL(url);
		} catch (MalformedURLException e) {
			throw new SuprsendException("Passed URL is not in desired URL format.", e);
		}
		String requestURI = parsedURL.getPath();
		String query = parsedURL.getQuery();
		if(query != null) {
			requestURI = String.format("%s?%s", requestURI, query);
		}
		return requestURI;
	}
}
