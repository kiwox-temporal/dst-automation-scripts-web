package net.kiwox.dst.script.command;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.FacebookTest;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.pojo.TestResult;

public class FacebookCommand extends AbstractCommand {

	private static final String USERNAME_OPTION = "user";
	private static final String PASSWORD_OPTION = "pass";
	private static final String FOLDER_NAME_OPTION = "folder";
	private static final String COMMENT_OPTION = "comment";
	
	private static final String DEFAULT_COMMENT = "Prueba publicacion facebook";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String user = commandLine.getOptionValue(USERNAME_OPTION);
			String pass = commandLine.getOptionValue(PASSWORD_OPTION);
			String folder = commandLine.getOptionValue(FOLDER_NAME_OPTION);
			String comment = commandLine.getOptionValue(COMMENT_OPTION, DEFAULT_COMMENT);
			ITest test = new FacebookTest(user, pass, folder, comment);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-FACEBOOK-003");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(USERNAME_OPTION)
				.longOpt("username")
				.hasArg()
				.required()
				.desc("Facebook user name (required)")
				.build());
		options.addOption(Option
				.builder(PASSWORD_OPTION)
				.longOpt("password")
				.hasArg()
				.required()
				.desc("Facebook password (required)")
				.build());
		options.addOption(Option
				.builder(FOLDER_NAME_OPTION)
				.longOpt("folder-name")
				.hasArg()
				.required(false)
				.desc("Facebook gallery folder with the picture to upload (if not set the default folder is used)")
				.build());
		options.addOption(Option
				.builder(COMMENT_OPTION)
				.hasArg()
				.required(false)
				.desc("Comment to publish in facebook. Default: \"" + DEFAULT_COMMENT + "\"")
				.build());
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "facebook";
	}

	@Override
	protected String getErrorCode() {
		return "DST-FACEBOOK-004";
	}

}
