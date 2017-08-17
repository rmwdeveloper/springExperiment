package com.example.demo;

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
	}

	public static void main(int httpPort) throws Exception {
		long start = System.currentTimeMillis();
		WebServer webServer = WebServer.initialize(httpPort);
		System.err.println("Web Server Started for development in " + (System.currentTimeMillis() - start) + " milliseconds .");
	}
}