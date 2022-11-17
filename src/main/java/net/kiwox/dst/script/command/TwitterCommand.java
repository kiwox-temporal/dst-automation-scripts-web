package net.kiwox.dst.script.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TwitterTest;
import net.kiwox.dst.script.pojo.TestResult;

public class TwitterCommand extends AbstractCommand {

	private static final String USERNAME_OPTION = "user";
	private static final String PASSWORD_OPTION = "pass";
	private static final String COMMENT_OPTION = "comment";
	private static final String ITERATIONS_OPTION = "it";
	
	private static final String DEFAULT_COMMENT = "Prueba twitter texto iteracion {0}";
	private static final int DEFAULT_ITERATIONS = 3;

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String user = commandLine.getOptionValue(USERNAME_OPTION);
			String pass = commandLine.getOptionValue(PASSWORD_OPTION);
			String comment = commandLine.getOptionValue(COMMENT_OPTION, DEFAULT_COMMENT);
			int iterations = Integer.parseInt(commandLine.getOptionValue(ITERATIONS_OPTION, "" + DEFAULT_ITERATIONS));
			ITest test = new TwitterTest(user, pass, comment, iterations);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-TWITTER-003");
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
				.desc("Twitter user name (required)")
				.build());
		options.addOption(Option
				.builder(PASSWORD_OPTION)
				.longOpt("password")
				.hasArg()
				.required()
				.desc("Twitter password (required)")
				.build());
		options.addOption(Option
				.builder(COMMENT_OPTION)
				.hasArg()
				.required(false)
				.desc("Comment to publish in twitter (\"{0}\" is replaced with the iteration number). Default: \"" + DEFAULT_COMMENT + "\"")
				.build());
		options.addOption(Option
				.builder(ITERATIONS_OPTION)
				.longOpt("iterations")
				.hasArg()
				.required(false)
				.desc("Amount of times to publish in twitter")
				.type(Number.class)
				.build());
		return options;
	}
	
	@Override
	protected boolean validateCommandLine(CommandLine commandLine) {
		try {
			commandLine.getParsedOptionValue(ITERATIONS_OPTION);
		} catch (ParseException e) {
			return false;
		}
		return super.validateCommandLine(commandLine);
	}

	@Override
	protected String getCommandName() {
		return "twitter";
	}

	@Override
	protected String getErrorCode() {
		return "DST-TWITTER-004";
	}

}
