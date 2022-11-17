package net.kiwox.dst.script.appium.facebook;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.functions.ExpectedCondition;

public class FacebookActivityExpectedCondition implements ExpectedCondition<String> {
	
	private String appActivity;
	private String loginActivity;
	private String deviceLoginActivity;
	private String loginPath;
	private String switchAccountPath;

	@SuppressWarnings("unchecked")
	@Override
	public String apply(WebDriver d) {
		AndroidDriver<AndroidElement> driver = (AndroidDriver<AndroidElement>) d;
		String current = driver.currentActivity();
		if (current.equals(appActivity)) {
			return appActivity;
		} else if (current.equals(loginActivity)) {
			return checkActivity(driver, loginPath, loginActivity);
		} else if (current.equals(deviceLoginActivity)) {
			return checkActivity(driver, switchAccountPath, deviceLoginActivity);
		}
		return null;
	}
	
	private String checkActivity(AndroidDriver<AndroidElement> driver, String path, String activity) {
		try {
			driver.findElementByXPath(path);
		} catch (NoSuchElementException e) {
			return null;
		}
		return activity;
	}

	public void setAppActivity(String appActivity) {
		this.appActivity = appActivity;
	}

	public void setLoginActivity(String loginActivity) {
		this.loginActivity = loginActivity;
	}

	public void setDeviceLoginActivity(String deviceLoginActivity) {
		this.deviceLoginActivity = deviceLoginActivity;
	}

	public void setLoginPath(String loginPath) {
		this.loginPath = loginPath;
	}

	public void setSwitchAccountPath(String switchAccountPath) {
		this.switchAccountPath = switchAccountPath;
	}

}
