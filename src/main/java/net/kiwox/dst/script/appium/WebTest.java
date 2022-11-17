package net.kiwox.dst.script.appium;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import net.kiwox.dst.script.pojo.TestResult;

public class WebTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebTest.class);
	private static final String APP_PACKAGE = "net.kiwox.dst.android";
	private static final String APP_ACTIVITY = ".MainActivity";
	private static final int APP_TIMEOUT = 300;
	
	private String[] urls;

	public WebTest(String[] urls) {
		this.urls = urls;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();
		
		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		StringBuilder args = new StringBuilder();
		args.append("-e test web --esa urls ");
		for (String url : urls) {
			args.append("\"");
			args.append(url);
			args.append("\",");
		}
		activity.setOptionalIntentArguments(StringUtils.stripEnd(args.toString(), ","));
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, APP_TIMEOUT);
		long startTime = System.currentTimeMillis();

		// Wait for result
		String resultPath = "//*[@resource-id='net.kiwox.dst.android:id/result']";
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resultPath)));
		} catch (TimeoutException | NoSuchElementException e) {
			LOGGER.error("Result text view not found. Timeout exceeded", e);
			result.setError(true);
			result.setCode("DST-WEB-006");
			TestUtils.saveScreenshot(driver);
			return result;
		}
		long time = System.currentTimeMillis() - startTime;

		AndroidElement resultText = driver.findElementByXPath(resultPath);
		String r = resultText.getText();
		if ("DST-WRONG-ARGS".equals(r)) {
			// Incorrect args
			result.setError(true);
			result.setCode("DST-WEB-004");
			TestUtils.saveScreenshot(driver);
		} else {
			try {
				JSONObject json = new JSONObject(r);
				if (json.has("err")) {
					// Data has error message
					result.setError(true);
					result.setCode("DST-WEB-005");
					result.setMessage(json.getString("err"));
					TestUtils.saveScreenshot(driver);
				} else if (json.has("results")) {
					// Data is correct
					result.setError(false);
					result.setMessage(json.getJSONArray("results").toString());
				} else {
					// Unexpected data, showing as error
					result.setError(true);
					result.setCode("DST-WEB-007");
					result.setMessage(r);
					TestUtils.saveScreenshot(driver);
				}
			} catch (JSONException e) {
				String err = "Error parsing JSON: " + r;
				LOGGER.error(err, e);
				result.setError(true);
				result.setCode("DST-WEB-007");
				result.setMessage(e.getMessage());
				TestUtils.saveScreenshot(driver);
			}
		}
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		result.setTime(time);
		return result;
	}

}
