package net.kiwox.dst.script.chrome.driver;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.mobile.NetworkConnection;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteLocationContext;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.openqa.selenium.remote.mobile.RemoteNetworkConnection;

import com.google.common.collect.ImmutableMap;

public class ChromeHttpDriver extends RemoteWebDriver implements LocationContext, WebStorage, HasTouchScreen, NetworkConnection {

	private RemoteLocationContext locationContext;
	private RemoteWebStorage webStorage;
	private TouchScreen touchScreen;
	private RemoteNetworkConnection networkConnection;

	/**
	 * Creates a new ChromeHttpDriver using the
	 * {@link ChromeDriverService#createDefaultService default} server
	 * configuration.
	 *
	 * @see #ChromeHttpDriver(ChromeDriverService, ChromeOptions)
	 */
	public ChromeHttpDriver() {
		this(ChromeDriverService.createDefaultService(), new ChromeOptions());
	}

	/**
	 * Creates a new ChromeHttpDriver instance. The {@code service} will be started
	 * along with the driver, and shutdown upon calling {@link #quit()}.
	 *
	 * @param service The service to use.
	 * @see RemoteWebDriver#RemoteWebDriver(org.openqa.selenium.remote.CommandExecutor,
	 *      Capabilities)
	 */
	public ChromeHttpDriver(ChromeDriverService service) {
		this(service, new ChromeOptions());
	}

	/**
	 * Creates a new ChromeHttpDriver instance with the specified options.
	 *
	 * @param options The options to use.
	 * @see #ChromeHttpDriver(ChromeDriverService, ChromeOptions)
	 */
	public ChromeHttpDriver(ChromeOptions options) {
		this(ChromeDriverService.createDefaultService(), options);
	}

	/**
	 * Creates a new ChromeHttpDriver instance with the specified options. The
	 * {@code service} will be started along with the driver, and shutdown upon
	 * calling {@link #quit()}.
	 *
	 * @param service The service to use.
	 * @param options The options to use.
	 */
	public ChromeHttpDriver(ChromeDriverService service, ChromeOptions options) {
		this(service, (Capabilities) options);
	}

	/**
	 * Creates a new ChromeHttpDriver instance. The {@code service} will be started
	 * along with the driver, and shutdown upon calling {@link #quit()}.
	 *
	 * @param service      The service to use.
	 * @param capabilities The capabilities required from the ChromeHttpDriver.
	 */
	private ChromeHttpDriver(ChromeDriverService service, Capabilities capabilities) {
		super(new ChromeDriverHttpCommandExecutor(service), capabilities);
		init();
	}
	
	/**
	 * Creates a new ChromeHttpDriver instance.
	 *
	 * @param url     The service URL.
	 * @param options The options to use.
	 */
	public ChromeHttpDriver(URL url, ChromeOptions options) {
		super(new ChromeDriverHttpCommandExecutor(url), options);
		init();
	}
	
	private void init() {
		locationContext = new RemoteLocationContext(getExecuteMethod());
		webStorage = new RemoteWebStorage(getExecuteMethod());
		touchScreen = new RemoteTouchScreen(getExecuteMethod());
		networkConnection = new RemoteNetworkConnection(getExecuteMethod());
	}

	@Override
	public void setFileDetector(FileDetector detector) {
		throw new WebDriverException("Setting the file detector only works on remote webdriver instances obtained via RemoteWebDriver");
	}

	@Override
	public LocalStorage getLocalStorage() {
		return webStorage.getLocalStorage();
	}

	@Override
	public SessionStorage getSessionStorage() {
		return webStorage.getSessionStorage();
	}

	@Override
	public Location location() {
		return locationContext.location();
	}

	@Override
	public void setLocation(Location location) {
		locationContext.setLocation(location);
	}

	@Override
	public TouchScreen getTouch() {
		return touchScreen;
	}

	@Override
	public ConnectionType getNetworkConnection() {
		return networkConnection.getNetworkConnection();
	}

	@Override
	public ConnectionType setNetworkConnection(ConnectionType type) {
		return networkConnection.setNetworkConnection(type);
	}

	/**
	 * Launches Chrome app specified by id.
	 *
	 * @param id Chrome app id.
	 */
	public void launchApp(String id) {
		execute(ChromeDriverHttpCommandExecutor.LAUNCH_APP, ImmutableMap.of("id", id));
	}
}
