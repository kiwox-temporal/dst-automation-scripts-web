package net.kiwox.dst.script.command;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.WhatsappTest;
import net.kiwox.dst.script.pojo.TestResult;

public class WhatsappPhotoCommand extends AbstractCommand {

	private static final String CONTACT_OPTION = "c";
	private static final String TEXT_OPTION = "t";
	private static final String FOLDER_NAME_OPTION = "folder";
	
	private static final String DEFAULT_TEXT = "Prueba whatsapp foto";

	@Override
	protected TestResult internalRunTest(String[] args) {
		if (parseCommandLine(args)) {
			String contact = commandLine.getOptionValue(CONTACT_OPTION);
			String text = commandLine.getOptionValue(TEXT_OPTION, DEFAULT_TEXT);
			String folder = commandLine.getOptionValue(FOLDER_NAME_OPTION);
			ITest test = new WhatsappTest(contact, text, folder);
			return startServer(test);
		}
		
		TestResult result = new TestResult();
		result.setError(true);
		result.setCode("DST-WAPHOTO-003");
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
		options.addOption(Option
				.builder(FOLDER_NAME_OPTION)
				.longOpt("folder-name")
				.hasArg()
				.required()
				.desc("Whatsapp gallery folder with the picture to upload (required)")
				.build());
		return options;
	}
	
	@Override
	protected String getCommandName() {
		return "whatsappPhoto";
	}

	@Override
	protected String getErrorCode() {
		return "DST-WAPHOTO-004";
	}

}
