package net.kiwox.dst.script.chrome.driver;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.HttpMethod;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

public class ChromeDriverHttpCommandExecutor extends HttpCommandExecutor {

	public static final String LAUNCH_APP = "launchApp";
	
	private static final String GET_NETWORK_CONDITIONS = "getNetworkConditions";
	private static final String SET_NETWORK_CONDITIONS = "setNetworkConditions";
	private static final String DELETE_NETWORK_CONDITIONS = "deleteNetworkConditions";
	private static final String NETWORK_CONDITIONS_URL = "/session/:sessionId/chromium/network_conditions";

	private static final ImmutableMap<String, CommandInfo> CHROME_COMMAND_NAME_TO_URL = ImmutableMap.of(
		LAUNCH_APP, new CommandInfo("/session/:sessionId/chromium/launch_app", HttpMethod.POST)
		, GET_NETWORK_CONDITIONS, new CommandInfo(NETWORK_CONDITIONS_URL, HttpMethod.GET)
		, SET_NETWORK_CONDITIONS, new CommandInfo(NETWORK_CONDITIONS_URL, HttpMethod.POST)
		, DELETE_NETWORK_CONDITIONS, new CommandInfo(NETWORK_CONDITIONS_URL, HttpMethod.DELETE)
	);
	
	private final ChromeDriverService service;

	public ChromeDriverHttpCommandExecutor(ChromeDriverService service) {
		super(CHROME_COMMAND_NAME_TO_URL, service.getUrl());
		this.service = service;
	}
	
	public ChromeDriverHttpCommandExecutor(URL url) {
		super(CHROME_COMMAND_NAME_TO_URL, url);
		this.service = null;
	}

	@Override
	public Response execute(Command command) throws IOException {
		if (service != null && DriverCommand.NEW_SESSION.equals(command.getName())) {
			service.start();
		}

		try {
			return super.execute(command);
		} catch (Exception t) {
			Throwable rootCause = Throwables.getRootCause(t);
			if (rootCause instanceof ConnectException && "Connection refused".equals(rootCause.getMessage())
					&& (service == null || !service.isRunning())) {
				throw new WebDriverException("The driver server has unexpectedly died!", t);
			}
			Throwables.throwIfUnchecked(t);
			throw new WebDriverException(t);
		} finally {
			if (service != null && DriverCommand.QUIT.equals(command.getName())) {
				service.stop();
			}
		}
	}

}
