package net.kiwox.dst.script.command.chrome;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.chrome.EntelWebBalanceTest;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.pojo.TestResult;

public class EntelWebBalanceCommand extends AbstractChromeCommand {

	private static final String PHONE_NUMBER_OPTION = "phone";
	private static final String PASS_CODE_OPTION = "code";
	
	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String phoneNumber = commandLine.getOptionValue(PHONE_NUMBER_OPTION);
			String passCode = commandLine.getOptionValue(PASS_CODE_OPTION);
			IChromeTest test = new EntelWebBalanceTest(phoneNumber, passCode);
			return startServer(test);
		}
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-ENTELWEBBALANCE-003");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(PHONE_NUMBER_OPTION)
				.longOpt("phoneNumber")
				.hasArg()
				.required()
				.desc("Phone number to login (required)")
				.build());
		options.addOption(Option
				.builder(PASS_CODE_OPTION)
				.longOpt("passCode")
				.hasArg()
				.required()
				.desc("Login verification code received by SMS (required). Must have at least 6 characters.")
				.build());
		return options;
	}

	@Override
	protected boolean validateCommandLine(CommandLine commandLine) {
		String passCode = commandLine.getOptionValue(PASS_CODE_OPTION, "");
		return passCode.length() > 5 && super.validateCommandLine(commandLine);
	}

	@Override
	protected String getCommandName() {
		return "entelWebBalance";
	}

	@Override
	protected String getErrorCode() {
		return "DST-ENTELWEBBALANCE-004";
	}

}
