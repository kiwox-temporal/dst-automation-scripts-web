package net.kiwox.dst.script.appium.instagram;

import org.openqa.selenium.WebDriver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.functions.ExpectedCondition;

public class InstagramImageExpectedCondition implements ExpectedCondition<AndroidElement> {
	
	private String imagePreviewPath;
	private String nextPath;

	public InstagramImageExpectedCondition(String imagePreviewPath, String nextPath) {
		this.imagePreviewPath = imagePreviewPath;
		this.nextPath = nextPath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AndroidElement apply(WebDriver d) {
		AndroidDriver<AndroidElement> driver = (AndroidDriver<AndroidElement>) d;
		driver.findElementByXPath(imagePreviewPath);
		return driver.findElementByXPath(nextPath);
	}

}
