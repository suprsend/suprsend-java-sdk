package suprsend;

import org.json.JSONObject;

/**
 * Holds optional application-level metadata (name, version) that the client
 * application can pass while initializing the SDK. This information is appended
 * to the User-Agent header.
 */
public class AppInfo {
	private String name;
	private String version;

	public AppInfo(String name) {
		this(name, null);
	}

	public AppInfo(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	/**
	 * Serialize this AppInfo to a JSON object containing only the populated
	 * fields. Returns null if both name and version are missing.
	 */
	JSONObject toJson() {
		JSONObject json = new JSONObject();
		if (this.name != null) {
			json.put("name", this.name);
		}
		if (this.version != null) {
			json.put("version", this.version);
		}
		return json;
	}

	/**
	 * Format AppInfo as "name" or "name/version" string. Returns empty string
	 * when name is missing.
	 */
	static String format(AppInfo info) {
		if (info == null || info.name == null || info.name.trim().isEmpty()) {
			return "";
		}
		String s = info.name.trim();
		if (info.version != null && !info.version.trim().isEmpty()) {
			s = s + "/" + info.version.trim();
		}
		return s;
	}
	
}
