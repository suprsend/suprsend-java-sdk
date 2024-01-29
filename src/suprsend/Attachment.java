package suprsend;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Creates attachment structure to be included in event/workflow
 */
public class Attachment {
	private static List<String> urlSchemes = Arrays.asList("https://", "http://");

	public static JSONObject getAttachmentJSON(String filePath) throws InputValueException {
		return getAttachmentJSON(filePath, null, false);
	}

	public static JSONObject getAttachmentJSON(String filePath, String fileName) throws InputValueException {
		return getAttachmentJSON(filePath, fileName, false);
	}

	public static JSONObject getAttachmentJSON(String filePath, String fileName, boolean ignoreIfError)
			throws InputValueException {
		// check for empty filepath
		if (filePath == null || filePath.trim().isEmpty()) {
			return null;
		}
		filePath = filePath.trim();
		// filename
		if (fileName == null || fileName.trim().isEmpty()) {
			fileName = null;
		} else {
			fileName = fileName.trim();
		}
		//
		if (checkIsWebUrl(filePath)) {
			return getAttachmentJSONForUrl(filePath, fileName, ignoreIfError);
		} else {
			return getAttachmentJSONForFile(filePath, fileName, ignoreIfError);
		}
	}

	private static boolean checkIsWebUrl(String filePath) {
		for (String s : urlSchemes) {
			if (filePath.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	private static JSONObject getAttachmentJSONForUrl(String fileUrl, String fileName, boolean ignoreIfError) {
		return new JSONObject()
				.put("filename", fileName)
				.put("contentType", JSONObject.NULL)
				.put("data", JSONObject.NULL)
				.put("url", fileUrl)
				.put("ignore_if_error", ignoreIfError);
	}

	private static JSONObject getAttachmentJSONForFile(String filePath, String fileName, boolean ignoreIfError)
			throws InputValueException {
		// Handle ~ in path. Ensure that path is expanded and absolute
		filePath = filePath.replaceFirst("^~", System.getProperty("user.home"));
		//
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				throw new InputValueException(String.format("file not found: %s", filePath));
			}
			String finalFileName = fileName != null ? fileName : file.getName();
			String mimeType = Files.probeContentType(file.toPath());
			//
			byte[] fileBytes = Files.readAllBytes(file.toPath());
			String b64EncodedStr = Base64.getEncoder().encodeToString(fileBytes);
			//
			return new JSONObject()
					.put("filename", finalFileName)
					.put("contentType", mimeType)
					.put("data", b64EncodedStr)
					.put("url", JSONObject.NULL)
					.put("ignore_if_error", ignoreIfError);
		} catch (Exception ex) {
			if (ignoreIfError) {
				System.out.println(
						String.format("WARNING: ignoring error while processing attachment file. %s", ex.toString()));
				return null;
			}
			throw new InputValueException(ex.toString());
		}
	}

}
