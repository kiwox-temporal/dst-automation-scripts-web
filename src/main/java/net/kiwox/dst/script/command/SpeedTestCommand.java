package net.kiwox.dst.script.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.SpeedTest;
import net.kiwox.dst.script.pojo.TestResult;

public class SpeedTestCommand extends AbstractCommand {

	private static final String ITERATIONS_OPTION = "it";
	
	private static final int DEFAULT_ITERATIONS = 3;
	
	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			int iterations = Integer.parseInt(commandLine.getOptionValue(ITERATIONS_OPTION, "" + DEFAULT_ITERATIONS));
			ITest test = new SpeedTest(iterations);
			return startServer(test);
		}
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-SPEEDTEST-003");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(ITERATIONS_OPTION)
				.longOpt("iterations")
				.hasArg()
				.required(false)
				.desc("Amount of times to execute SpeedTest. Default to " + DEFAULT_ITERATIONS)
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
		return "speedtest";
	}

	@Override
	protected String getErrorCode() {
		return "DST-SPEEDTEST-004";
	}

}
