package net.kiwox.dst.script.appium;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.offset.PointOption;
import net.kiwox.dst.script.pojo.TestResult;

public class TwitterTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTest.class);
	private static final String APP_PACKAGE = "com.twitter.android";
	private static final String APP_ACTIVITY = ".StartActivity";
	private static final String SIGN_IN_ACTIVITY = "com.twitter.onboarding.ocf.loading.OcfStartFlowActivity";
	private static final String MAIN_ACTIVITY = "com.twitter.app.main.MainActivity";
	private static final String ROOT_PATH = "//*[@resource-id='com.twitter.android:id/root_coordinator_layout']";
	
	private String username;
	private String password;
	private String comment;
	private int iterations;

	public TwitterTest(String username, String password, String comment, int iterations) {
		this.username = username;
		this.password = password;
		this.comment = comment;
		this.iterations = iterations;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();

		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		activity.setAppWaitPackage(APP_PACKAGE);
		activity.setAppWaitActivity("com.twitter.*");
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		
		String current = driver.currentActivity();
		if (SIGN_IN_ACTIVITY.equals(current)) {
			/*
			 * Sign in screen
			 * Get the small text at the bottom and click the anchor on the right side
			 */
			String contentPath = "//*[@resource-id='android:id/content']";
			String signInPath = contentPath + "//*[@resource-id='com.twitter.android:id/detail_text']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(signInPath)));
			AndroidElement signIn = driver.findElementByXPath(signInPath);
			Point signInPoint = signIn.getCenter().moveBy(signIn.getRect().getWidth() / 2, 0);
			new TouchAction<>(driver).press(new PointOption<>().withCoordinates(signInPoint)).release().perform();
			
			// Login screen
			String userPath = contentPath + "//*[@resource-id='com.twitter.android:id/login_identifier']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(userPath)));
			new Actions(driver).sendKeys(username).perform();
			AndroidElement user = driver.findElementByXPath(userPath);
			// Click outside to avoid auto complete
			Point userPoint = user.getCenter();
			userPoint.move(0, userPoint.getY());
			new TouchAction<>(driver).press(new PointOption<>().withCoordinates(userPoint)).release().perform();
			
			String passPath = contentPath + "//*[@resource-id='com.twitter.android:id/login_password']";
			driver.findElementByXPath(passPath).click();
			new Actions(driver).sendKeys(password).perform();
			AndroidElement pass = driver.findElementByXPath(passPath);
			// Click outside to avoid auto complete
			Point passPoint = pass.getCenter();
			passPoint.move(0, passPoint.getY());
			new TouchAction<>(driver).press(new PointOption<>().withCoordinates(passPoint)).release().perform();
			
			String loginPath = contentPath + "//*[@resource-id='com.twitter.android:id/login_login']";
			driver.findElementByXPath(loginPath).click();
		} else if (!MAIN_ACTIVITY.equals(current)) {
			LOGGER.info("Unexpected activity start: {}", current);
			result.setError(true);
			result.setCode("DST-TWITTER-005");
			result.setMessage(current);
			TestUtils.saveScreenshot(driver);
			return result;
		}
		
		/*
		 * Main screen:
		 * - Click "Timeline" button (top right sparks icon)
		 * - Get title from popup
		 * - Click title to sort tweets by date (or back if already sorted)
		 * - Wait for "Write" button to appear (feather icon)
		 */
		String timelinePath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/toolbar_timeline_switch']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(timelinePath))).click();
		String timelineTitlePath = "//*[@resource-id='android:id/content']//*[@resource-id='com.twitter.android:id/action_sheet_item_title']";
		WebElement timelineTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(timelineTitlePath)));
		String timelineTitleText = timelineTitle.getText();
		switch (timelineTitleText.toLowerCase()) {
			case "switch to home":
			case "go back home":
			case "cambiar a inicio":
			case "volver a inicio":
				driver.pressKey(new KeyEvent(AndroidKey.BACK));
				break;
			default: 
				timelineTitle.click(); 
				break;
		}
		
		String snackPath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/snackbar_text']";
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(snackPath)));
		
		// driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		
		goToStart(driver, wait);
		
		String writePath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/composer_write']";
		// String writePath = "//*[@id='composer_write']";
		// String writePath = "//*[@resource-id='com.twitter.android:id/composer_write']";
		// driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(writePath)));
		
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long[] tweetTimes = new long[iterations];
		long startTime = System.currentTimeMillis();
		boolean errorInSomeTest = false;
		for (int i = 0; i < iterations; ++i) {
			try {
				// Click "Write" button
				driver.findElementByXPath(writePath).click();
				
				/*
				 * New tweet screen:
				 * - Fill text field (selected automatically)
				 * - Add current date to message to avoid duplicates
				 * - Click "Tweet" button
				 */
				try {
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
					// En caso de existir un segundo boton
					driver.findElementByXPath(writePath).click();
					// wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(textEditPath)));
				} catch (Exception e) {
					// Segundo boton?
					// driver.findElementByXPath(writePath).click();
					// driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				}
				String composerPath = "//*[@resource-id='com.twitter.android:id/composer']";
				String textEditPath = composerPath + "//*[@resource-id='com.twitter.android:id/tweet_text']";
				
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(textEditPath)));
				
				
				String fullComment = comment.replace("{0}", "" + (i+1)) + " " + dateFormat.format(new Date());
				new Actions(driver).sendKeys(fullComment).perform();
				String sendPath = composerPath + "//*[@resource-id='com.twitter.android:id/button_tweet']";
				driver.findElementByXPath(sendPath).click();
				tweetTimes[i] = System.currentTimeMillis();
				
				/*
				 * Main screen:
				 * - Wait for screen to appear and progress bar to disappear
				 * - Click "Home" button to scroll back to top
				 * - Get text from last tweet and validate
				 */
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(ROOT_PATH)));
				String progressPath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/main_progress_bar']";
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(progressPath)));
				tweetTimes[i] = System.currentTimeMillis() - tweetTimes[i];
				goToStart(driver, wait);
				String rowPath = ROOT_PATH + "//*[@resource-id='android:id/list']//*[@resource-id='com.twitter.android:id/row']";
				String text = driver.findElementByXPath(rowPath).getAttribute("content-desc");
				if (!validateText(fullComment, text)) {
					LOGGER.info("Last message validation error. Expected [\"{}\"], got: [\"{}\"]", fullComment, text);
					result.setError(true);
					result.setCode("DST-TWITTER-006");
					result.setMessage(text);
					TestUtils.saveScreenshot(driver);
					return result;
				}
			} catch (Exception e) {
				LOGGER.error("Unexpected error during twitter test", e);
				TestUtils.saveScreenshot(driver);
				if (reset(driver, wait)) {
					LOGGER.info("Twitter test iteration {} marked with error. Continues with the next one", i+1);
					tweetTimes[i] = 0;
					errorInSomeTest = true;
				} else {
					LOGGER.info("Couldn't reset twitter test screen. Finishing twitter test");
					driver.pressKey(new KeyEvent(AndroidKey.HOME));
					result.setError(true);
					result.setCode("DST-TWITTER-004");
					result.setMessage(e.getMessage());
					return result;
				}
			}
		}
		long time = System.currentTimeMillis() - startTime;
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		Gson gson = new Gson();
		result.setError(errorInSomeTest);
		result.setTime(time);
		result.setMessage(gson.toJson(tweetTimes, long[].class));
		return result;
	}
	
	private boolean goToStart(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		String channelsPath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/channels']";
		String logoPath = ROOT_PATH + "//*[@resource-id='com.twitter.android:id/logo']";
		
		AndroidElement channelsButton = null;
		try {
			channelsButton = driver.findElementByXPath(channelsPath);
			channelsButton.click();
		} catch (NoSuchElementException e) {
			// NO OP
		}
		
		if (channelsButton == null) {
			try {
				driver.findElementByXPath(logoPath).click();
				return true;
			} catch (NoSuchElementException e) {
				// NO OP
			}
			return false;
		}
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(logoPath)));
		return true;
	}
	
	private boolean validateText(String textSent, String textRead) {
		if (textRead == null) {
			return false;
		}
		
		String[] split = textRead.split(". . . ");
		if (split.length < 2) {
			return false;
		}
		
		return split[1].replace("hashtag ", "#").equals(textSent);
	}
	
	private boolean reset(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		String anyTwitterPath = "//*[starts-with(@resource-id, 'com.twitter.android')]";
		do {
			if (goToStart(driver, wait)) {
				return true;
			}
			driver.pressKey(new KeyEvent(AndroidKey.BACK));
			driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		} while (!driver.findElementsByXPath(anyTwitterPath).isEmpty());
		return false;
	}

}
