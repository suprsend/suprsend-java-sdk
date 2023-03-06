package suprsend;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Initialize HTTP request logs when debug is sent as true
 * 
 * @author Suprsend
 */
class RequestLogs {
	// static {
	// ConsoleHandler handler = new ConsoleHandler();
	// handler.setLevel(Level.FINE);
	// // sun.net.www.protocol.http.HttpUrlConnection
	// Logger log = LogManager.getLogManager().getLogger("");
	// log.addHandler(handler);
	// log.setLevel(Level.FINE);
	// }

	private static void logHttpCall(Logger logger, HttpMethod httpMethod, String url, JSONObject headers, String payload) {
		logger.log(Level.INFO,
				String.format(
						"HTTP Request \n------------------------------->>\n"
								+ "METHOD:\t%s\nURL:\t%s\nHEADER:\t%s\nBODY:\t%s\n" + "------------------------------->>",
						httpMethod.name(), url, headers.toString(), payload));
	}

	private static void logHttpResponse(Logger logger, int statusCode, String contentType, String responseText){
		logger.log(Level.INFO,
				String.format(
						"HTTP Response \n<<-------------------------------\n"
								+ "Status Code:\t%d\nContent-Type:\t%s\nResponse:\t%s\n" + "<<-------------------------------",
						statusCode, contentType, responseText));

	}

	private static void setMandatoryHeaders(HttpURLConnection httpConn, JSONObject headers) {
		httpConn.setRequestProperty("Content-Type", headers.getString("Content-Type"));
		httpConn.setRequestProperty("User-Agent", headers.getString("User-Agent"));
		httpConn.setRequestProperty("Date", headers.getString("Date"));
		if (headers.opt("Authorization") != null) {
			httpConn.setRequestProperty("Authorization", headers.getString("Authorization"));
		}
	}

	static SuprsendResponse makeHttpCall(Logger logger, boolean debug, HttpMethod httpMethod, String url,
										JSONObject headers, String payload) throws IOException {
		if (debug) {
			logHttpCall(logger, httpMethod, url, headers, payload);
		}
		// --- Make HTTP POST request
		HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
		httpConn.setRequestMethod(httpMethod.name());
		setMandatoryHeaders(httpConn, headers);
		if (httpMethod != HttpMethod.GET) {
			httpConn.setDoOutput(true);
			byte[] input = payload.getBytes(StandardCharsets.UTF_8);
			try (DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream())) {
				dos.write(input);
			}
		}
		//
		int statusCode = httpConn.getResponseCode();
		BufferedReader br = null;
		if (statusCode >= 400) {
			br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
		} else {
			br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		}
		String respText = br.lines().collect(Collectors.joining());
		String contentType = httpConn.getContentType();
		if (debug) {
			logHttpResponse(logger, statusCode, contentType, respText);
		}
		//
		SuprsendResponse response = new SuprsendResponse(statusCode, respText, contentType);
		response.parseResponse();
		return response;
	}
}
