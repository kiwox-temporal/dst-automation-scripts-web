package net.kiwox.dst.script.appium.custom;

import io.appium.java_client.service.local.flags.ServerArgument;

public enum CustomServerFlag implements ServerArgument {
	/**
     * Allow insecure features like shell.
     */
    ALLOW_INSECURE("--allow-insecure");

    private final String arg;
	
	private CustomServerFlag(String arg) {
		this.arg = arg;
	}

	@Override
	public String getArgument() {
		return arg;
	}

}
