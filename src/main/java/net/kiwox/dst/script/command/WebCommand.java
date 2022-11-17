package net.kiwox.dst.script.command;

import org.apache.commons.cli.CommandLine;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.WebTest;
import net.kiwox.dst.script.pojo.TestResult;

public class WebCommand extends AbstractCommand {

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			ITest test = new WebTest(commandLine.getArgs());
			return startServer(test);
		}
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-WEB-003");
		return result;
	}

	@Override
	protected boolean validateCommandLine(CommandLine commandLine) {
		return super.validateCommandLine(commandLine) && commandLine.getArgs().length > 0;
	}

	@Override
	protected String getHeader() {
		return "Add extra arguments for URLs to query (required)";
	}

	@Override
	protected String getCommandName() {
		return "web";
	}

	@Override
	protected String getErrorCode() {
		return "DST-WEB-005";
	}

}
