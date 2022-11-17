package net.kiwox.dst.script.command.chrome.testflows;

import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.testflows.EntelWebVerifyPostPaidBagTest;
import net.kiwox.dst.script.command.chrome.AbstractChromeCommand;
import net.kiwox.dst.script.pojo.TestResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import static net.kiwox.dst.script.appium.TestUtils.*;

public class EntelWebVerfiyPostPaidBagCommand extends AbstractChromeCommand {

	private static final String PHONE_NUMBER_OPTION = "phone";
	private static final String PASS_CODE_OPTION = "code";
	
	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String phoneNumber = commandLine.getOptionValue(PHONE_NUMBER_OPTION);
			String passCode = commandLine.getOptionValue(PASS_CODE_OPTION);
			IChromeTest test = new EntelWebVerifyPostPaidBagTest(phoneNumber, passCode);
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
		return 	CLI_EXECUTE_VERIFY_POST_PAID_BAG_02_ENTEL_WEB_APP;
	}

	@Override
	protected String getErrorCode() {
		return "DST-ENTEL-WEB-002";
	}

}
