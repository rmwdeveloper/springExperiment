package com.example.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;

import org.apache.commons.lang3.StringUtils;
import com.example.common.util.net.HostnameUtil;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

public final class WebServer {

	public static int MAX_HEADER_BUFFER_SIZE = 8192;
	private static WebServer instance;
	private int httpPort;
	private Server jetty;
	private volatile Status status = Status.STOPPED;

	private AnnotationConfigApplicationContext webServerContent;

	private MBeanServer mbeanServer;
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
		this.status = Status.STARTING;

		MetricRegistry metricRegistry = new MetricRegistry();
		HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
		InstrumentedQueuedThreadPool threadPool = new InstrumentedQueuedThreadPool(metricRegistry, 512, 32);

		this.webServerContent = new AnnotationConfigApplicationContext();
		this.webServerContent.refresh();

		this.webServerContent.start();
		this.jetty = new Server(threadPool);

		MBeanContainer mbeanContainer = new MBeanContainer(this.mbeanServer);
		this.jetty.addEventListener(mbeanContainer);
		this.jetty.addBean(mbeanContainer);

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSendDateHeader(false);
		httpConfig.setSendServerVersion(false);
		httpConfig.setSendXPoweredBy(false);
		httpConfig.setHeaderCacheSize(WebServer.MAX_HEADER_BUFFER_SIZE);



		InstrumentedConnectionFactory httpInstrumentedConnectionFactory = new InstrumentedConnectionFactory(new HttpConnectionFactory(httpConfig), new Timer());
		NetworkTrafficServerConnector httpConnector = new NetworkTrafficServerConnector(this.jetty, httpInstrumentedConnectionFactory);
		httpConnector.setHost("0.0.0.0");
		httpConnector.setPort(this.httpPort);
		this.jetty.addConnector(httpConnector);


		WebAppContext webAppContext = new WebAppContext("WebContent", "/");
		webAppContext.setDefaultsDescriptor("conf/webdefault.xml");
		webAppContext.setCopyWebDir(false);
		webAppContext.setThrowUnavailableOnStartupException(true); // If the context does not start correct (maybe because of Spring) fail
		webAppContext.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
		webAppContext.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);




		HandlerCollection handlers = new HandlerCollection();
		Handler[] handlerArray;
		handlerArray = new Handler[] { webAppContext, new DefaultHandler() };
		handlers.setHandlers(handlerArray);


		webAppContext.getServletHandler().setStartWithUnavailable(false); // If a servlet does not start then fail

		this.jetty.setStopAtShutdown(true);
		try {
			this.jetty.start();
		} catch (Exception jettyStartupError) {
			System.exit(255); // NOPMD Infrastructure
		}


		this.status = Status.STARTED;
	}

	public void stop() throws Exception {
		this.status = Status.STOPPING;
		if (this.jetty != null) {
			this.jetty.stop();
		}
		this.jetty = null;
		if (this.webServerContent != null) {
			this.webServerContent.stop();
		}
		this.status = Status.STOPPED;
	}

	public static void main(String[] args) throws Exception {

		List<String> argList = new ArrayList<>(Arrays.asList(args));

		int httpPort = Integer.parseInt(argList.get(0));

		long start = System.currentTimeMillis();
		WebServer webServer = WebServer.initialize(httpPort);
		webServer.start();
		System.err.println("Web Server Started for development in " + (System.currentTimeMillis() - start) + " milliseconds .");
	}
	public enum Status {
		STARTING,
		STARTED,
		STOPPING,
		STOPPED
	}
}