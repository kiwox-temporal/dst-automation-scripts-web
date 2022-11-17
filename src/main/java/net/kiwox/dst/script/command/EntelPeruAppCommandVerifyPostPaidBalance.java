package net.kiwox.dst.script.command;

import net.kiwox.dst.script.appium.entel_peru.EntelPeruAppTestVerifyPostPaidBalance;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.pojo.TestResult;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class EntelPeruAppCommandVerifyPostPaidBalance extends AbstractCommand {

	
	private static final String PHONE_NUMBER_APP_ENTEL = "phone";
	private static final String VERIFICATION_CODE_APP_ENTEL = "code";
	private static final String FOLDER_NAME_APP_ENTEL = "folder";
	private static final String COMMENT_OPTION = "comment";
	
	private static final String DEFAULT_COMMENT = "Prueba publicacion facebook";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String phoneNumber = commandLine.getOptionValue(PHONE_NUMBER_APP_ENTEL);
			String verificationCode = commandLine.getOptionValue(VERIFICATION_CODE_APP_ENTEL);
			ITest test = new EntelPeruAppTestVerifyPostPaidBalance(phoneNumber, verificationCode);
			return startServer(test);
		}
		// TODO: Review response of below line
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-MI-ENTEL-PERU-002");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(PHONE_NUMBER_APP_ENTEL)
				.longOpt("phoneNumber")
				.hasArg()
				.required()
				.desc("Entel App Phone number (required)")
				.build());
		options.addOption(Option
				.builder(VERIFICATION_CODE_APP_ENTEL)
				.longOpt("verificationCode")
				.hasArg()
				.required()
				.desc("Entel App Verification Code (required)")
				.build());
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "app-entel-peru-verificar-saldo-post-pago";
	}

	@Override
	protected String getErrorCode() {
		return "DST-MI-ENTEL-PERU-002";
	}
	
	
}
