package net.kiwox.dst.script.command;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.gson.Gson;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import net.kiwox.dst.script.ICommand;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.appium.custom.CustomServerFlag;
import net.kiwox.dst.script.pojo.TestResult;

public abstract class AbstractCommand implements ICommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);

    // Options
    private static final String HELP_OPTION = "h";
    private static final String PORT_OPTION = "p";
    private static final String UDID_OPTION = "udid";
    private static final String NODE_OPTION = "node";
    private static final String APPIUM_OPTION = "appium";
    private static final String ANDROID_OPTION = "android";
    private static final String WAIT_TIMEOUT_OPTION = "wt";
    private static final String SCREENSHOT_OPTION = "screenshot";
    private static final String TEST_RESULT_ID_OPTION = "tr";

    // Defaults
    private static final String DEFAULT_NODE_PATH = "/usr/local/bin/node"; // NOSONAR
    //private static final String DEFAULT_NODE_PATH = "/Users/msaavedra/.nvm/versions/node/v18.6.0/bin/node"; // NOSONAR
    private static final String DEFAULT_APPIUM_PATH = "/usr/local/bin/appium"; // NOSONAR
    private static final String DEFAULT_ANDROID_PATH = "/usr/local/android-sdk,/usr/lib/android-sdk"; // NOSONAR
    //private static final String DEFAULT_ANDROID_PATH = "/Users/msaavedra/Library/Android/sdk"; // NOSONAR
    private static final long DEFAULT_WAIT_TIMEOUT = 20;

    private static final String DEFAULT_SCREENSHOT_PATH = "/tmp"; // NOSONAR
    private static final long DEFAULT_TEST_RESULT_ID = 0;

    // Server
    private static final String JAVA_HOME_PROPERTY = "java.home";
    private static final String JAVA_HOME_KEY = "JAVA_HOME";
    private static final String ANDROID_HOME_KEY = "ANDROID_HOME";

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
                .builder(PORT_OPTION)
                .longOpt("port")
                .hasArg()
                .required(false)
                .desc("Appium server port. Defaults to any random available port")
                .type(Number.class)
                .build());
        options.addOption(Option
                .builder(UDID_OPTION)
                .hasArg()
                .required()
                .desc("Device UDID (required)")
                .build());
        options.addOption(Option
                .builder(NODE_OPTION)
                .longOpt("nodePath")
                .hasArg()
                .required(false)
                .desc("Path to node executable. Default to " + DEFAULT_NODE_PATH)
                .build());
        options.addOption(Option
                .builder(APPIUM_OPTION)
                .longOpt("appiumPath")
                .hasArg()
                .required(false)
                .desc("Path to appium executable. Default to " + DEFAULT_APPIUM_PATH)
                .build());
        options.addOption(Option
                .builder(ANDROID_OPTION)
                .longOpt("androidPath")
                .hasArg()
                .required(false)
                .desc("Paths to Android SDK separated by comma. Default to " + DEFAULT_ANDROID_PATH)
                .build());
        options.addOption(Option
                .builder(WAIT_TIMEOUT_OPTION)
                .longOpt("waitTimeout")
                .hasArg()
                .required(false)
                .desc("Time in seconds to wait for objects to appear in Appium tests. Default to " + DEFAULT_WAIT_TIMEOUT)
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
        } catch (ParseException e) {
            line = null;
        }
        if (line == null || !validateCommandLine(line)) {
            HelpFormatter help = new HelpFormatter();
            System.out.println("Help Parsing : " + line.getArgs());
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

    protected TestResult startServer(ITest test) {
        String nodeOpt = commandLine.getOptionValue(NODE_OPTION, DEFAULT_NODE_PATH);
        File node = new File(nodeOpt);
        if (!node.exists()) {
            throw new IllegalArgumentException("Node executable not found at " + nodeOpt);
        }
        if (!node.canExecute()) {
            throw new IllegalArgumentException("Node file found but it's not executable at " + nodeOpt);
        }

        String appiumOpt = commandLine.getOptionValue(APPIUM_OPTION, DEFAULT_APPIUM_PATH);
        File appium = new File(appiumOpt);
        if (!appium.exists()) {
            throw new IllegalArgumentException("Appium executable not found at " + appiumOpt);
        }
        if (!appium.canExecute()) {
            throw new IllegalArgumentException("Appium file found but it's not executable at " + appiumOpt);
        }

        String android = null;
        String androidPaths = commandLine.getOptionValue(ANDROID_OPTION, DEFAULT_ANDROID_PATH);
        for (String a : androidPaths.split(",")) {
            File androidSdk = new File(a);
            if (androidSdk.exists() && androidSdk.isDirectory()) {
                android = a;
                break;
            }
        }
        if (android == null) {
            throw new IllegalArgumentException("Android SDK directory not found at " + androidPaths);
        }

        String waitTimeout = commandLine.getOptionValue(WAIT_TIMEOUT_OPTION, "" + DEFAULT_WAIT_TIMEOUT);
        TestUtils.setWaitTimeout(Long.parseLong(waitTimeout));

        StringBuilder screenshotName = new StringBuilder();
        screenshotName.append(commandLine.getOptionValue(TEST_RESULT_ID_OPTION, "" + DEFAULT_TEST_RESULT_ID));
        screenshotName.append(' ');
        screenshotName.append(commandLine.getOptionValue(UDID_OPTION));
        screenshotName.append(' ');
        screenshotName.append(getCommandName());
        Path screenshotPath = Paths.get(commandLine.getOptionValue(SCREENSHOT_OPTION, DEFAULT_SCREENSHOT_PATH), screenshotName.toString());
        TestUtils.setScreenshotFilePath(screenshotPath.toString() + " ?????.png");

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        AppiumServiceBuilder serviceBuilder = new AppiumServiceBuilder();
        serviceBuilder.usingDriverExecutable(node);
        serviceBuilder.withAppiumJS(appium);
        if (allowInsecureFeatures()) {
            serviceBuilder.withArgument(CustomServerFlag.ALLOW_INSECURE, "adb_shell");
        }

        String port = commandLine.getOptionValue(PORT_OPTION);
        if (port == null) {
            serviceBuilder.usingAnyFreePort();
        } else {
            serviceBuilder.usingPort(Integer.parseInt(port));
        }

        HashMap<String, String> environment = new HashMap<>();
        environment.put(ANDROID_HOME_KEY, android);
        environment.put(JAVA_HOME_KEY, System.getProperty(JAVA_HOME_PROPERTY));
        serviceBuilder.withEnvironment(environment);

        AppiumDriverLocalService server = AppiumDriverLocalService.buildService(serviceBuilder);
        server.clearOutPutStreams();
        server.enableDefaultSlf4jLoggingOfOutputData();
        server.start();

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability(MobileCapabilityType.UDID, commandLine.getOptionValue(UDID_OPTION));

        AndroidDriver<AndroidElement> driver = null;
        try {
            driver = new AndroidDriver<>(server.getUrl(), dc);
            driver.setLogLevel(Level.INFO);
            return test.runTest(driver);
        } catch (Exception e) {
            TestUtils.saveScreenshot(driver);
            LOGGER.error("Error creating or running Android driver", e);
            TestResult result = new TestResult();
            result.setError(true);
            result.setCode(getErrorCode());
            result.setMessage(e.getMessage());
            return result;
        } finally {
            if (driver != null) {
                driver.quit();
            }
            server.stop();
        }
    }

    protected boolean allowInsecureFeatures() {
        return false;
    }

    protected abstract TestResult internalRunTest(String[] args);

    protected abstract String getCommandName();

    protected abstract String getErrorCode();

}
