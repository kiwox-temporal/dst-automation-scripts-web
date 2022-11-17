package net.kiwox.dst.script.command;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.WhatsappTest;
import net.kiwox.dst.script.pojo.TestResult;

public class WhatsappTextCommand extends AbstractCommand {

	private static final String CONTACT_OPTION = "c";
	private static final String TEXT_OPTION = "t";
	
	private static final String DEFAULT_TEXT = "Prueba whatsapp texto";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String contact = commandLine.getOptionValue(CONTACT_OPTION);
			String text = commandLine.getOptionValue(TEXT_OPTION, DEFAULT_TEXT);
			ITest test = new WhatsappTest(contact, text);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-WATEXT-003");
		return result;
	}

	@Override
	protected Options buildOptions() {
		Options options = super.buildOptions();
		options.addOption(Option
				.builder(CONTACT_OPTION)
				.longOpt("contact")
				.hasArg()
				.required()
				.desc("Whatsapp contact (required)")
				.build());
		options.addOption(Option
				.builder(TEXT_OPTION)
				.longOpt("text")
				.hasArg()
				.required(false)
				.desc("Text to send in whatsapp. Default: \"" + DEFAULT_TEXT + "\"")
				.build());
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "whatsappText";
	}

	@Override
	protected String getErrorCode() {
		return "DST-WATEXT-004";
	}

}
