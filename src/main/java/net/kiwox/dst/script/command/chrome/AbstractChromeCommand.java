package net.kiwox.dst.script.command.chrome;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.gson.Gson;

import net.kiwox.dst.script.ICommand;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.pojo.TestResult;

public abstract class AbstractChromeCommand implements ICommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChromeCommand.class);
	
	// Options
	private static final String HELP_OPTION = "h";
	private static final String HOST_OPTION = "host";
	private static final String PORT_OPTION = "p";
	private static final String WAIT_TIMEOUT_OPTION = "wt";
	private static final String SCREENSHOT_OPTION = "screenshot";
	private static final String TEST_RESULT_ID_OPTION = "tr";
	
	// Defaults
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 9515;
	private static final long DEFAULT_WAIT_TIMEOUT = 20;
	private static final String DEFAULT_SCREENSHOT_PATH = "/tmp"; // NOSONAR
	private static final long DEFAULT_TEST_RESULT_ID = 0;
	
	protected CommandLine commandLine;
	
	@Override
	public void runTest(String[] args) {
		TestResult result = internalRunTest(args);
		Gson gson = new Gson();
		System.out.println("DST Test Result"); // NOSONAR
		System.out.println(gson.toJson(result)); // NOSONAR
	}
	
	protected Options buildOptions() {
		Options options = new Options();
		options.addOption(Option
				.builder(HELP_OPTION)
				.longOpt("help")
				.hasArg(false)
				.required(false)
				.desc("Show help")
				.build());
		options.addOption(Option
				.builder(HOST_OPTION)
				.hasArg()
				.required(false)
				.desc("Remote device host. Default to " + DEFAULT_HOST)
				.build());
		options.addOption(Option
				.builder(PORT_OPTION)
				.longOpt("port")
				.hasArg()
				.required(false)
				.desc("Remote device port. Default to " + DEFAULT_PORT)
				.type(Number.class)
				.build());
		options.addOption(Option
				.builder(WAIT_TIMEOUT_OPTION)
				.longOpt("waitTimeout")
				.hasArg()
				.required(false)
				.desc("Time in seconds to wait for objects to appear in Chrome tests. Default to " + DEFAULT_WAIT_TIMEOUT)
				.type(Number.class)
				.build());
		options.addOption(Option
				.builder(SCREENSHOT_OPTION)
				.longOpt("screenshotPath")
				.hasArg()
				.required(false)
				.desc("Path to save screenshots. Default to " + DEFAULT_SCREENSHOT_PATH)
				.build());
		options.addOption(Option
				.builder(TEST_RESULT_ID_OPTION)
				.longOpt("testResultId")
				.hasArg()
				.required(false)
				.desc("Test result ID. Default to " + DEFAULT_TEST_RESULT_ID)
				.type(Number.class)
				.build());
		return options;
	}
	
	protected boolean parseCommandLine(String[] args) {
		Options options = buildOptions();
		CommandLine line;
		try {
			line = new DefaultParser().parse(options, args);
			line.getParsedOptionValue(PORT_OPTION);
			line.getParsedOptionValue(WAIT_TIMEOUT_OPTION);
			line.getParsedOptionValue(TEST_RESULT_ID_OPTION);
			System.out.println(line);
		} catch (ParseException e) {
			line = null;
			System.out.println(e.getMessage());
		}
		if (line == null || !validateCommandLine(line)) {
			HelpFormatter help = new HelpFormatter();
			help.printHelp("java -jar dst_test.jar " + getCommandName(), getHeader(), options, null);
			return false;
		}
		this.commandLine = line;
		return true;
	}
	
	protected boolean validateCommandLine(CommandLine commandLine) {
		return !commandLine.hasOption(HELP_OPTION);
	}
	
	protected String getHeader() {
		return null;
	}
	
	protected TestResult startServer(IChromeTest test) {
    	String waitTimeout = commandLine.getOptionValue(WAIT_TIMEOUT_OPTION, "" + DEFAULT_WAIT_TIMEOUT);
    	TestUtils.setWaitTimeout(Long.parseLong(waitTimeout));
    	
    	StringBuilder screenshotName = new StringBuilder();
    	screenshotName.append(commandLine.getOptionValue(TEST_RESULT_ID_OPTION, "" + DEFAULT_TEST_RESULT_ID));
    	screenshotName.append(' ');
    	screenshotName.append(commandLine.getOptionValue(HOST_OPTION, DEFAULT_HOST));
    	screenshotName.append(' ');
    	screenshotName.append(getCommandName());
    	Path screenshotPath = Paths.get(commandLine.getOptionValue(SCREENSHOT_OPTION, DEFAULT_SCREENSHOT_PATH), screenshotName.toString());
    	TestUtils.setScreenshotFilePath(screenshotPath.toString() + " ?????.png");
    	
    	SLF4JBridgeHandler.removeHandlersForRootLogger();
    	SLF4JBridgeHandler.install();
    	
    	ChromeOptions options = new ChromeOptions();
    	options.addArguments("start-maximized");
        
    	ChromeHttpDriver driver = null;
        try {
        	URL url = new URIBuilder()
    			.setScheme("http")
    			.setHost(commandLine.getOptionValue(HOST_OPTION, DEFAULT_HOST))
    			.setPort(Integer.parseInt(commandLine.getOptionValue(PORT_OPTION, "" + DEFAULT_PORT)))
    			.build()
    			.toURL();
			driver = new ChromeHttpDriver(url, options);
			driver.setLogLevel(Level.INFO);
			return test.runTest(driver);
		} catch (Exception e) {
			e.printStackTrace();
			TestUtils.saveScreenshot(driver);
			LOGGER.error("Error creating or running Chrome driver", e);
			TestResult result = new TestResult();
			result.setError(true);
			result.setCode(getErrorCode());
			result.setMessage(e.getMessage());
			return result;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		} 
	}
	
	protected abstract TestResult internalRunTest(String[] args);
	
	protected abstract String getCommandName();
	
	protected abstract String getErrorCode();

}
