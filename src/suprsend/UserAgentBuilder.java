package suprsend;

import org.json.JSONObject;

/**
 * Builds the User-Agent header value and the X-Suprsend-Client-User-Agent
 * header value (a JSON-serialized object describing the SDK environment).
 */
class UserAgentBuilder {
	// Result holder for the two header values produced by build().
	static class UserAgent {
		final String userAgent;
		final String clientUserAgent;

		UserAgent(String userAgent, String clientUserAgent) {
			this.userAgent = userAgent;
			this.clientUserAgent = clientUserAgent;
		}
	}

	static UserAgent build(AppInfo appInfo) {
		String os, osVersion;
		try {
			os = System.getProperty("os.name", "");
			osVersion = System.getProperty("os.version", "");
		} catch (Exception e) {
			os = "(disabled)";
			osVersion = "(disabled)";
		}
		String langVersion = System.getProperty("java.version", "");
		JSONObject ins = new JSONObject()
				.put("sdk", "suprsend-java-sdk")
				.put("sdk_version", Version.VERSION)
				.put("lang", "java")
				.put("lang_version", langVersion)
				.put("platform", "server")
				.put("os", os)
				.put("os_version", osVersion);
		//
		if (appInfo != null) {
			ins.put("app_info", appInfo.toJson());
		}
		String clientUserAgent = ins.toString();

		String userAgent = String.format("suprsend-java-sdk/%s (java/%s; %s)", Version.VERSION, langVersion, os);
		String appInfoStr = AppInfo.format(appInfo);
		if (!appInfoStr.isEmpty()) {
			userAgent = userAgent + " (" + appInfoStr + ")";
		}
		return new UserAgent(userAgent, clientUserAgent);
	}
}
