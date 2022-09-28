package suprsend;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

/**
 * This class creates signature of the payload of HTTP request to SuprSend
 * platform. Signature is required to detect tempering of the request.
 * 
 * @author Suprsend
 *
 */
class Signature {
	private static final String HMAC_SHA256 = "HmacSHA256";

	private static Mac getSha256macInstance(String secret) throws SuprsendException {
		final byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		SecretKeySpec keySpec = new SecretKeySpec(secretBytes, HMAC_SHA256);
		Mac sha256mac;
		try {
			sha256mac = Mac.getInstance(HMAC_SHA256);
			sha256mac.init(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new SuprsendException("Invalid algorithm name passed.", e);
		} catch (InvalidKeyException e) {
			throw new SuprsendException("Invalid key passed.", e);
		}
		return sha256mac;
	}

	/**
	 * Get request signature
	 * 
	 * @param url      Workflow backend URL
	 * @param httpVerb HTTP method
	 * @param content  Raw content
	 * @param headers  Raw headers
	 * @param secret   Workspace secret key given to client by Suprsend
	 * @return JSON object which contains raw content in string format and
	 *         signature.
	 * @throws SuprsendException if error occurs while creating signature
	 */
	public static JSONObject getRequestSignature(String url, String httpVerb, JSONObject content, JSONObject headers,
			String secret) throws SuprsendException {
		//
		Mac sha256mac = getSha256macInstance(secret);
		//
		String contentTxt = "";
		String contentMD5 = "";
		// In case of GET request, there is no payload body,
		// so assume contentTxt and contentMD5 to be empty.
		if (!"GET".equals(httpVerb)) {
			contentTxt = content.toString();
			contentMD5 = getMD5(contentTxt);
		}
		String requestURI = getURI(url);
		// Create string to sign
		String stringToSign = String.format("%s\n%s\n%s\n%s\n%s", httpVerb, contentMD5,
				headers.get("Content-Type").toString(), headers.get("Date").toString(), requestURI);
		//
		byte[] macData = sha256mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
		String signature = Base64.getEncoder().encodeToString(macData);
		//
		JSONObject result = new JSONObject().put("contentTxt", contentTxt).put("signature", signature);
		return result;
	}

	/**
	 * Get request signature
	 * 
	 * @param url      Workflow backend URL
	 * @param httpVerb HTTP method
	 * @param content  Raw content as list of dictionary
	 * @param headers  Raw headers
	 * @param secret   Workspace secret given to client by Suprsend
	 * @return JSON object which contains raw content in string format and
	 *         signature.
	 * @throws SuprsendException if error occurs while creating signature
	 */
	public static JSONObject getRequestSignature(String url, String httpVerb, List<JSONObject> content,
			JSONObject headers, String secret) throws SuprsendException {
		//
		Mac sha256mac = getSha256macInstance(secret);
		//
		String contentTxt = "";
		String contentMD5 = "";
		// In case of GET request, there is no payload body,
		// so assume contentTxt and contentMD5 to be empty.
		if (!"GET".equals(httpVerb)) {
			contentTxt = content.toString();
			contentMD5 = getMD5(contentTxt);
		}
		String requestURI = getURI(url);
		// Create string to sign
		String stringToSign = String.format("%s\n%s\n%s\n%s\n%s", httpVerb, contentMD5,
				headers.get("Content-Type").toString(), headers.get("Date").toString(), requestURI);
		//
		byte[] macData = sha256mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
		String signature = Base64.getEncoder().encodeToString(macData);
		//
		JSONObject result = new JSONObject().put("contentTxt", contentTxt).put("signature", signature);
		return result;
	}

	/**
	 * Get MD5 hash digest for body
	 * 
	 * @param input String format of JSON body.
	 * @return MD5 hash digest of passed input.
	 */
	private static String getMD5(String input) {
		String md5hex = DigestUtils.md5Hex(input);
		return md5hex;
	}

	/**
	 * URI with passed query string
	 * 
	 * @param url Passed URL
	 * @return URI
	 * @throws SuprsendException
	 */
	private static String getURI(String url) throws SuprsendException {
		URL parsedURL;
		try {
			parsedURL = new URL(url);
		} catch (MalformedURLException e) {
			throw new SuprsendException("not a valid URL format.", e);
		}
		String requestURI = parsedURL.getPath();
		String query = parsedURL.getQuery();
		if (query != null) {
			requestURI = String.format("%s?%s", requestURI, query);
		}
		return requestURI;
	}
}
