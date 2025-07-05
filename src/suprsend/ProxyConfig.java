package suprsend;

import java.net.Proxy;
import java.net.URL;

public class ProxyConfig {
    private String host;
    private int port;
    private Proxy.Type type;
    private String authType; // BASIC, future: NTLM, DIGEST, etc.
    private String username;
    private String password;
    // private String domain;

    public ProxyConfig(String host, int port, Proxy.Type type, String authType, String username, String password) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.authType = authType != null ? authType : "BASIC"; // Default to BASIC
        this.username = username;
        this.password = password;
        // this.domain = domain;
    }

    // Constructor for no authentication
    public ProxyConfig(String host, int port, Proxy.Type type) {
        this(host, port, type, "BASIC", null, null);
    }

    private static String getENV(String[] envs) {
        String val = null;
        for (String envVar : envs) {
            String lowerEnvVar = envVar.toLowerCase();
            String upperEnvVar = envVar.toUpperCase();
            val = System.getenv(lowerEnvVar);
            if (val != null && !val.trim().isEmpty()) {
                break;
            }
            val = System.getenv(upperEnvVar);
            if (val != null && !val.trim().isEmpty()) {
                break;
            }
        }
        return val;
    }

    // Factory method to create ProxyConfig from environment variables
    public static ProxyConfig fromEnvironment() {
        String username = null;
        String password = null;
        String proxyUrl = getENV(new String[] {"HTTP_PROXY", "HTTPS_PROXY"});
        if (proxyUrl == null || proxyUrl.trim().isEmpty()) {
            return null; // No proxy configured
        }
        try {
            // Parse proxy URL (e.g., http://proxy.example.com:8080 or proxy.example.com:8080)
            String host;
            int port;
            Proxy.Type type = Proxy.Type.HTTP; // Default to HTTP
            if (proxyUrl.startsWith("http://") || proxyUrl.startsWith("https://")) {
                URL url = new URL(proxyUrl);
                host = url.getHost();
                port = url.getPort() != -1 ? url.getPort() : 8080; // Default port
                type = Proxy.Type.HTTP;
                String userInfo = url.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] authParts = userInfo.split(":", 2);
                    if (authParts.length == 2) {
                        username = authParts[0];
                        password = authParts[1];
                    }
                }
            } else if (proxyUrl.startsWith("socks://") || proxyUrl.startsWith("socks5://") || proxyUrl.startsWith("socks5h://")) {
                URL url = new URL("http://" + proxyUrl.substring(proxyUrl.indexOf("://") + 3));
                host = url.getHost();
                port = url.getPort() != -1 ? url.getPort() : 1080; // Default SOCKS port
                type = Proxy.Type.SOCKS;
                String userInfo = url.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] authParts = userInfo.split(":", 2);
                    if (authParts.length == 2) {
                        username = authParts[0];
                        password = authParts[1];
                    }
                }
            } else {
                String[] parts = proxyUrl.split(":");
                host = parts[0];
                port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8080;
            }
            String authType = getENV(new String[] {"HTTP_PROXY_AUTH_TYPE", "HTTPS_PROXY_AUTH_TYPE", "PROXY_AUTH_TYPE"});
            if (username == null || username.isEmpty()) {
                username = getENV(new String[] {"HTTP_PROXY_USER", "HTTPS_PROXY_USER", "PROXY_USER"});
            }
            if (password == null || password.isEmpty()) {
                password = getENV(new String[] {"HTTP_PROXY_PASS", "HTTPS_PROXY_PASS", "HTTP_PROXY_PASSWORD", "HTTPS_PROXY_PASSWORD", "PROXY_PASSWORD"});
            }
            // String domain = getENV(new String[] {"HTTP_PROXY_DOMAIN", "HTTPS_PROXY_DOMAIN", "PROXY_DOMAIN"});

            return new ProxyConfig(host, port, type, authType, username, password);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid proxy configuration in environment variables: " + proxyUrl, e);
        }
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public Proxy.Type getType() { return type; }
    public String getAuthType() { return authType; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    // public String getDomain() { return domain; }
    public boolean hasCredentials() { return username != null && password != null; }
}
