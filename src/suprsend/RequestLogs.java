package suprsend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.client.HttpClients;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;


class CustomHttpClient {
    private static CloseableHttpClient client;

    static synchronized CloseableHttpClient getInstance() {
		if (client == null) {
			RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(30000)
				.setSocketTimeout(60000)
				.build();

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        	cm.setMaxTotal(100);
        	CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(5L, TimeUnit.MINUTES)
				.setDefaultRequestConfig(config)
                .build();
			client = httpclient;
		}
		return client;
    }
}

/**
 * Initialize HTTP request logs when debug is sent as true
 * 
 * @author Suprsend
 */
class RequestLogs {

	private static void logHttpCall(Logger logger, HttpMethod httpMethod, String url, JSONObject headers,
			String payload) {
		logger.log(Level.INFO,
				String.format("HTTP Request \n------------------------------->>\n"
						+ "METHOD:\t%s\nURL:\t%s\nHEADER:\t%s\nBODY:\t%s\n" + "------------------------------->>",
						httpMethod.name(), url, headers.toString(), payload));
	}

	private static void logHttpResponse(Logger logger, int statusCode, String contentType, String responseText) {
		logger.log(Level.INFO,
				String.format("HTTP Response \n<<-------------------------------\n"
						+ "Status Code:\t%d\nContent-Type:\t%s\nResponse:\t%s\n" + "<<-------------------------------",
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
			CloseableHttpClient httpClient = CustomHttpClient.getInstance();
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
			case DELETE:
				httpRequest = new HttpDeleteWithBody(url);
				if (payload != null && !payload.isEmpty()) {
					((HttpDeleteWithBody) httpRequest).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
			}

			// Set headers using the setMandatoryHeaders method
			setMandatoryHeaders(httpRequest, headers);

			// Execute the request
			response = httpClient.execute(httpRequest);

			// Read the response
			String contentType = "";
			String respText = "";
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity respEntity = response.getEntity();
			if (null != respEntity) {
				if (null != respEntity.getContentType()) {
					contentType = respEntity.getContentType().getValue();
				}
				InputStream ct = respEntity.getContent();
				if (null != ct) {
					try (BufferedReader br = new BufferedReader(new InputStreamReader(ct))) {
						respText = br.lines().collect(Collectors.joining());
					}
				}
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

class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

    public static final String METHOD_NAME = "DELETE";
	
	public HttpDeleteWithBody() { 
		super(); 
	}
	
	public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }
	
	public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

	@Override
    public String getMethod() {
		return METHOD_NAME; 
	}
}
