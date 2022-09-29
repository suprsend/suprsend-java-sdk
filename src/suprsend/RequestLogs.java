package suprsend;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * Initialize HTTP request logs when debug is sent as true
 * 
 * @author Suprsend
 */
public class RequestLogs {
	// static {
	// ConsoleHandler handler = new ConsoleHandler();
	// handler.setLevel(Level.FINE);
	// // sun.net.www.protocol.http.HttpUrlConnection
	// Logger log = LogManager.getLogManager().getLogger("");
	// log.addHandler(handler);
	// log.setLevel(Level.FINE);
	// }

	protected static void logHttpCall(Logger logger, String method, String url, JSONObject headers, String payload) {
		logger.log(Level.INFO,
				String.format(
						"HTTP Request -------------------------------\n"
								+ "METHOD:\t%s\nURL:\t%s\nHEADER:\t%s\nBODY:\t%s\n" + "-------------------------------",
						"POST", url, headers.toString(), payload));
	}

	private static void setMandatoryHeaders(HttpURLConnection httpConn, JSONObject headers) {
		httpConn.setRequestProperty("Content-Type", headers.getString("Content-Type"));
		httpConn.setRequestProperty("User-Agent", headers.getString("User-Agent"));
		httpConn.setRequestProperty("Date", headers.getString("Date"));
		if (headers.opt("Authorization") != null) {
			httpConn.setRequestProperty("Authorization", headers.getString("Authorization"));
		}
	}

	protected static SuprsendResponse makeHttpCall(Logger logger, boolean debug, String method, String url,
			JSONObject headers, String payload) throws IOException {
		//
		if (debug) {
			logHttpCall(logger, method, url, headers, payload);
		}
		// --- Make HTTP POST request
		HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
		httpConn.setRequestMethod("POST");
		setMandatoryHeaders(httpConn, headers);
		httpConn.setDoOutput(true);
		//
		byte[] input = payload.getBytes(StandardCharsets.UTF_8);
		try (DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream())) {
			dos.write(input);
		}
		//
		int statusCode = httpConn.getResponseCode();
		String responseText = httpConn.getResponseMessage();
		return new SuprsendResponse(statusCode, responseText);
	}
}

class SuprsendResponse {
	int statusCode;
	String responseText;

	SuprsendResponse(int statusCode, String responseText) {
		this.statusCode = statusCode;
		this.responseText = responseText;
	}
}
