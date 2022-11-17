package net.kiwox.dst.script.command;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.ApnTest;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.pojo.TestResult;

public class ApnCommand extends AbstractCommand {
	
	private static final String APN_OPTION = "apn";
	private static final String PING_OPTION = "ping";
	
	private static final String DEFAULT_PING = "127.0.0.1";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String apn = commandLine.getOptionValue(APN_OPTION);
			String ping = commandLine.getOptionValue(PING_OPTION, DEFAULT_PING);
			ITest test = new ApnTest(apn, ping);
			return startServer(test);
		}
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-SELAPN-003");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(APN_OPTION)
				.hasArg()
				.desc("APN to configure in the device")
				.build());
		options.addOption(Option
				.builder(PING_OPTION)
				.hasArg()
				.desc("Address to check ping. Default to " + DEFAULT_PING)
				.build());
		return options;
	}
	
	@Override
	protected boolean allowInsecureFeatures() {
		return true;
	}

	@Override
	protected String getCommandName() {
		return "apn";
	}

	@Override
	protected String getErrorCode() {
		return "DST-SELAPN-004";
	}

}
