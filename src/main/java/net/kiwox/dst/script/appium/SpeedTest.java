package net.kiwox.dst.script.appium;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import net.kiwox.dst.script.pojo.SpeedTestResult;
import net.kiwox.dst.script.pojo.TestResult;

public class SpeedTest implements ITest {
	
	private static final String APP_PACKAGE = "org.zwanoo.android.speedtest";
	private static final String APP_ACTIVITY = "com.ookla.mobile4.screens.main.MainActivity";
	private static final String APP_WAIT_ACTIVITY = "com.ookla.mobile4.screens.main.*";
	private static final int TEST_TIMEOUT_STEP = 30;
	private static final int TEST_TIMEOUT = 10 * TEST_TIMEOUT_STEP;
	private static final String GO_PATH = "//*[@resource-id='org.zwanoo.android.speedtest:id/go_button' or @resource-id='org.zwanoo.android.speedtest:id/suite_completed_feedback_assembly_test_again']";
	private static final String ERROR_DIALOG_PATH = "//*[@resource-id='org.zwanoo.android.speedtest:id/buttonPanel']//*[@resource-id='android:id/button1']";
	private static final Logger LOGGER = LoggerFactory.getLogger(SpeedTest.class);
	
	private int iterations;
	
	public SpeedTest(int iterations) {
		this.iterations = iterations;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		activity.setAppWaitPackage(APP_PACKAGE);
		activity.setAppWaitActivity(APP_WAIT_ACTIVITY);
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		WebDriverWait testWait = new WebDriverWait(driver, TEST_TIMEOUT_STEP);

		// Wait to load
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(GO_PATH)));
		
		List<SpeedTestResult> speedResults = new LinkedList<>();
		String downloadPath = "//*[@resource-id='org.zwanoo.android.speedtest:id/download_result_view']//*[@resource-id='org.zwanoo.android.speedtest:id/txt_test_result_value']";
		String uploadPath = "//*[@resource-id='org.zwanoo.android.speedtest:id/upload_result_view']//*[@resource-id='org.zwanoo.android.speedtest:id/txt_test_result_value']";
		String pingPath = "//*[@resource-id='org.zwanoo.android.speedtest:id/test_result_item_ping']//*[@resource-id='org.zwanoo.android.speedtest:id/txt_test_result_value']";
		long startTime = System.currentTimeMillis();
		
		boolean errorInSomeTest = false;
		for (int i = 0; i < iterations; ++i) {
			try {
				// Start speed test and wait to finish
				driver.findElementByXPath(GO_PATH).click();
				waitInSteps(driver, testWait);
	
				// Get results
				AndroidElement download = driver.findElementByXPath(downloadPath);
				AndroidElement upload = driver.findElementByXPath(uploadPath);
				AndroidElement ping = driver.findElementByXPath(pingPath);
				
				SpeedTestResult r = new SpeedTestResult();
				r.setDownload(Float.parseFloat(download.getText()));
				r.setUpload(Float.parseFloat(upload.getText()));
				
				String p = StringUtils.stripEnd(ping.getText(), "ms");
				r.setPing(Long.parseLong(p));
				
				speedResults.add(r);
			} catch (Exception e) {
				LOGGER.error("Unexpected error during speed test", e);
				TestUtils.saveScreenshot(driver);
				if (reset(driver, wait)) {
					LOGGER.info("Speed test iteration {} marked with error. Continues with the next one", i+1);
					speedResults.add(new SpeedTestResult());
					errorInSomeTest = true;
				} else {
					LOGGER.info("Couldn't reset speed test screen. Finishing speed test");
					driver.pressKey(new KeyEvent(AndroidKey.HOME));
					TestResult result = new TestResult();
					result.setError(true);
					result.setCode("DST-SPEEDTEST-004");
					result.setMessage(e.getMessage());
					return result;
				}
			}
		}
		long time = System.currentTimeMillis() - startTime;
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		Gson gson = new Gson();
		TestResult result = new TestResult();
		result.setTime(time);
		result.setError(errorInSomeTest);
		result.setMessage(gson.toJson(speedResults, List.class));
		return result;
	}
	
	private void waitInSteps(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		WebDriverException exception = null;
		for (int t = 0; t < TEST_TIMEOUT; t += TEST_TIMEOUT_STEP) {
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(GO_PATH)));
				return;
			} catch (WebDriverException e) {
				exception = e;
			}
			
			try {
				driver.findElementByXPath(ERROR_DIALOG_PATH);
				throw exception;
			} catch (NoSuchElementException e) {
				// Error dialog not found, try again
			}
		}
		if (exception != null) {
			throw exception;
		}
	}
	
	private boolean reset(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		while (driver.findElementsByXPath(GO_PATH).isEmpty()) {
			AndroidElement closeButton = null;
			try {
				closeButton = driver.findElementByXPath("//*[@resource-id='org.zwanoo.android.speedtest:id/closeIcon']");
			} catch (NoSuchElementException e) {
				// Not found
			}
			
			if (closeButton == null) {
				try {
					closeButton = driver.findElementByXPath(ERROR_DIALOG_PATH);
				} catch (NoSuchElementException e) {
					// Not found
				}
			}
			
			if (closeButton == null) {
				return false;
			}
			
			closeButton.click();
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(GO_PATH)));
			} catch (WebDriverException e) {
				// "Go" button still not found, try again
			}
		}
		return true;
	}

}
