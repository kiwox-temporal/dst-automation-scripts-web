package net.kiwox.dst.script.command;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TikTokTest;
import net.kiwox.dst.script.pojo.TestResult;

public class TikTokCommand extends AbstractCommand {

	private static final String USERNAME_OPTION = "user";
	private static final String FOLDER_NAME_OPTION = "folder";
	private static final String COMMENT_OPTION = "comment";
	
	private static final String DEFAULT_COMMENT = "Prueba publicacion tikTok";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String user = commandLine.getOptionValue(USERNAME_OPTION);
			String folder = commandLine.getOptionValue(FOLDER_NAME_OPTION);
			String comment = commandLine.getOptionValue(COMMENT_OPTION, DEFAULT_COMMENT);
			ITest test = new TikTokTest(user, folder, comment);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-TIKTOK-003");
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
				.desc("Tiktok user name prefix (required)")
				.build());
		options.addOption(Option
				.builder(FOLDER_NAME_OPTION)
				.longOpt("folder-name")
				.hasArg()
				.required(false)
				.desc("TikTok gallery folder with the video to upload (if not set the default folder is used)")
				.build());
		options.addOption(Option
				.builder(COMMENT_OPTION)
				.hasArg()
				.required(false)
				.desc("Comment to publish in TikTok. Default: \"" + DEFAULT_COMMENT + "\"")
				.build());
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "tiktok";
	}

	@Override
	protected String getErrorCode() {
		return "DST-TIKTOK-004";
	}

}
