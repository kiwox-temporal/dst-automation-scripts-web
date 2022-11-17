package net.kiwox.dst.script.appium;

import org.json.JSONArray;
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

public class LoadTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoadTest.class);
	private static final String APP_PACKAGE = "net.kiwox.dst.android";
	private static final String APP_ACTIVITY = ".MainActivity";
	private static final int APP_TIMEOUT = 1200;
	
	private boolean upload;
	private String url;
	private String filePath;
	private int iterations;

	public LoadTest(boolean upload, String url, String filePath, int iterations) {
		this.upload = upload;
		this.url = url;
		this.filePath = filePath;
		this.iterations = iterations;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();
		
		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		StringBuilder args = new StringBuilder();
		args.append("-e test ");
		args.append(getType());
		args.append(" -e url \"");
		args.append(url);
		args.append("\" -e file \"");
		args.append(filePath);
		args.append("\"");
		args.append(" --ei iterations ");
		args.append(iterations);
		activity.setOptionalIntentArguments(args.toString());
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
			result.setCode(getCode("006"));
			return result;
		}
		long time = System.currentTimeMillis() - startTime;

		AndroidElement resultText = driver.findElementByXPath(resultPath);
		String r = resultText.getText();
		if ("DST-WRONG-ARGS".equals(r)) {
			// Incorrect args
			result.setError(true);
			result.setCode(getCode("004"));
		} else {
			try {
				JSONObject json = new JSONObject(r);
				if (json.has("err")) {
					// Data has error message
					result.setError(true);
					result.setCode(getCode("005"));
					result.setMessage(json.getString("err"));
				} else if (json.has("time") && json.has("size")) {
					// Data is correct
					result.setError(false);
					JSONArray timeArr = json.getJSONArray("time");
					LOGGER.debug("JSON ARRAY {}", timeArr);
					for (int i = 0; i < timeArr.length(); ++i) {
						long t = timeArr.getLong(i);
						LOGGER.debug("Comparando con {}", t);
						if (t == 0) {
							// data vacia
							LOGGER.debug("ERROR TRUE");
							result.setError(true);
						}
					}
					result.setMessage(r);
				} else {
					// Unexpected data, showing as error
					result.setError(true);
					result.setCode(getCode("007"));
					result.setMessage(r);
				}
			} catch (JSONException e) {
				String err = "Error parsing JSON: " + r;
				LOGGER.error(err, e);
				result.setError(true);
				result.setCode(getCode("007"));
				result.setMessage(e.getMessage());
			}
		}
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		result.setTime(time);
		return result;
	}
	
	private String getType() {
		return upload ? "upload" : "download";
	}
	
	private String getCode(String suffix) {
		return "DST-" + getType().toUpperCase() + "-" + suffix;
	}

}
