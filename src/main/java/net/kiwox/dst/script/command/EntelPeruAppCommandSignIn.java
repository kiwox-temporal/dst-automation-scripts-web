package net.kiwox.dst.script.command;

import net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.entel_peru.EntelPeruAppTestSignIn;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.pojo.TestResult;

public class EntelPeruAppCommandSignIn extends AbstractCommand {

	
	private static final String PHONE_NUMBER_APP_ENTEL = "phone";
	private static final String VERIFICATION_CODE_APP_ENTEL = "code";
	private static final String POPUP_CLOSE_X = "popup_x";
	private static final String POPUP_CLOSE_Y = "popup_y";

	private static final String FOLDER_NAME_APP_ENTEL = "folder";
	private static final String COMMENT_OPTION = "comment";
	
	private static final String DEFAULT_COMMENT = "Prueba publicacion facebook";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String phoneNumber = commandLine.getOptionValue(PHONE_NUMBER_APP_ENTEL);
			String verificationCode = commandLine.getOptionValue(VERIFICATION_CODE_APP_ENTEL);
			Integer popupCloseX = Integer.parseInt(commandLine.getOptionValue(POPUP_CLOSE_X));
			Integer popupCloseY = Integer.parseInt(commandLine.getOptionValue(POPUP_CLOSE_Y));
			ITest test = new EntelPeruAppTestSignIn(phoneNumber, verificationCode, popupCloseX, popupCloseY);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-MI-ENTEL-PERU-001");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		Options popupOptios = HelperEntelPeruApp.buildOptionsPopup();
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
		options.addOption(popupOptios.getOption(POPUP_CLOSE_X));
		options.addOption(popupOptios.getOption(POPUP_CLOSE_Y));
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "app-entel-peru-inicio-sesion";
	}

	@Override
	protected String getErrorCode() {
		return "DST-MI-ENTEL-PERU-001";
	}
	
	
}
