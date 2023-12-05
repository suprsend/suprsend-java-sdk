package suprsend;

import org.json.JSONObject;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		        String.format("HTTP Request \n------------------------------->>\n"
		                + "METHOD:\t%s\nURL:\t%s\nHEADER:\t%s\nBODY:\t%s\n"
		                + "------------------------------->>",
		                httpMethod.name(), url, headers.toString(), payload));
	}

	private static void logHttpResponse(Logger logger, int statusCode, String contentType, String responseText){
		logger.log(Level.INFO,
		        String.format("HTTP Response \n<<-------------------------------\n"
		                + "Status Code:\t%d\nContent-Type:\t%s\nResponse:\t%s\n"
		                + "<<-------------------------------",
						statusCode, contentType, responseText));
	}

	private static void setMandatoryHeaders(HttpRequestBase httpRequest, JSONObject headers) {
		httpRequest.setHeader("Content-Type", headers.getString("Content-Type"));
		httpRequest.setHeader("User-Agent", headers.getString("User-Agent"));
		httpRequest.setHeader("Date", headers.getString("Date"));

		if (headers.opt("Authorization") != null) {
			httpRequest.setHeader("Authorization", headers.getString("Authorization"));
		}
	}

	static SuprsendResponse makeHttpCall(Logger logger, boolean debug, HttpMethod httpMethod, String url,
                                         JSONObject headers, String payload) throws IOException {
        if (debug) {
            logHttpCall(logger, httpMethod, url, headers, payload);
        }

        CloseableHttpResponse response = null;

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpRequestBase httpRequest;

            switch (httpMethod) {
                case GET:
                    httpRequest = new HttpGet(url);
                    break;
                case POST:
                    httpRequest = new HttpPost(url);
                    ((HttpPost) httpRequest).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                    break;
                case PATCH:
                    httpRequest = new HttpPatch(url);
                    ((HttpPatch) httpRequest).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
            }

            // Set headers using the setMandatoryHeaders method
            setMandatoryHeaders(httpRequest, headers);

            // Execute the request
            response = (CloseableHttpResponse) httpClient.execute(httpRequest);

            // Read the response
            int statusCode = response.getStatusLine().getStatusCode();
            String contentType = response.getEntity().getContentType().getValue();
            String respText;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                respText = br.lines().collect(Collectors.joining());
            }

            if (debug) {
                logHttpResponse(logger, statusCode, contentType, respText);
            }

            // Close the response
            response.close();

            SuprsendResponse suprsendResponse = new SuprsendResponse(statusCode, respText, contentType);
            suprsendResponse.parseResponse();
            return suprsendResponse;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }        
}
