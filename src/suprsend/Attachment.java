package suprsend;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import org.json.JSONObject;

/**
 * Extracts file content from filePath and prepares desired attachment structure
 */
public class Attachment {

	public static JSONObject getAttachmentJSONForFile(String filePath) throws Exception {
		// Handle ~ in path
		filePath = filePath.replaceFirst("^~", System.getProperty("user.home"));
		//
		File file = new File(filePath);
		if (!file.exists()) {
			throw new SuprsendException(String.format("file not found: %s", filePath));
		}
		String fileName = file.getName();
		String mimeType = Files.probeContentType(file.toPath());
		//
		byte[] data = Files.readAllBytes(file.toPath());
		String encodedString = Base64.getEncoder().encodeToString(data);
		//
		return new JSONObject().put("filename", fileName).put("contentType", mimeType).put("data", encodedString);
	}
}
