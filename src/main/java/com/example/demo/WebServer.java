package com.example.demo;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import com.example.common.util.net.HostnameUtil;

public final class WebServer {

	private static WebServer instance;
	private int httpPort;

	private static WebServer initialize(int httpPort) throws Exception {
		WebServer webServer = new WebServer(httpPort);
		WebServer.instance = webServer;
		return webServer;
	}

	private WebServer(int httpPort) throws Exception {
		this.httpPort = httpPort;

		String hostname = HostnameUtil.getMyHostName();
		if (StringUtils.isBlank(hostname)) {
			hostname = HostnameUtil.getMyInetAddress().toString();
		}
		System.setProperty("MACHINEIDENT", hostname);
		File confDirectory = new File("WebContent/WEB-INF/conf");
	}

	private void start() throws Exception {

	}
	public static void main(int httpPort) throws Exception {
		long start = System.currentTimeMillis();
		WebServer webServer = WebServer.initialize(httpPort);
		webServer.start();
		System.err.println("Web Server Started for development in " + (System.currentTimeMillis() - start) + " milliseconds .");
	}
}