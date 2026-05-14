package test;

import java.net.Proxy;

import suprsend.AppInfo;
import suprsend.ProxyConfig;
import suprsend.Suprsend;
import suprsend.SuprsendException;

public class TestHelper {

	public static Suprsend getClientInstance() throws SuprsendException {
		String apiKey = "apikey";
		String apiSecret = "apiSecret";
		Suprsend suprClient = new Suprsend(apiKey, apiSecret);
		// Set appinfo (for logging purpose)
		suprClient.setAppInfo(new AppInfo("MyApp", "0.1.0"));
		return suprClient;
	}

	public static Suprsend getClientInstanceWithProxyEnv() throws SuprsendException {
		String apiKey = "apikey";
		String apiSecret = "apiSecret";
		// from ENV
		ProxyConfig proxyConf = ProxyConfig.fromEnvironment();
		Suprsend suprClient = new Suprsend(apiKey, apiSecret);
		suprClient.setProxyConfig(proxyConf);
		return suprClient;
	}

	public static Suprsend getClientInstanceWithManualProxyConfig() throws SuprsendException {
		String apiKey = "apikey";
		String apiSecret = "apiSecret";
		// manually ProxyConfig
		ProxyConfig proxyConf = new ProxyConfig("proxy.example.com", 8080, Proxy.Type.HTTP);
		Suprsend suprClient = new Suprsend(apiKey, apiSecret);
		suprClient.setProxyConfig(proxyConf);
		return suprClient;
	}
}
