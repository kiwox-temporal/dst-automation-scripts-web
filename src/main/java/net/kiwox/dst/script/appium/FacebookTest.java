package net.kiwox.dst.script.appium;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.offset.PointOption;
import net.kiwox.dst.script.appium.facebook.FacebookActivityExpectedCondition;
import net.kiwox.dst.script.pojo.TestResult;

public class FacebookTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FacebookTest.class);
	private static final String APP_PACKAGE = "com.facebook.katana";
	private static final String APP_ACTIVITY = ".activity.FbMainTabActivity";
	private static final String LOGIN_ACTIVITY = "com.facebook.account.login.activity.SimpleLoginActivity";
	private static final String DEVICE_LOGIN_ACTIVITY = ".dbl.activity.DeviceBasedLoginActivity";
	private static final int EDIT_BUTTONS_TIMEOUT = 10;
	private static final int UPLOAD_TIMEOUT = 300;
	
	private String username;
	private String password;
	private String folderName;
	private String comment;

	public FacebookTest(String username, String password, String folderName, String comment) {
		this.username = username;
		this.password = password;
		this.folderName = folderName;
		this.comment = comment;
	}



	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();
		
		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		activity.setAppWaitPackage(APP_PACKAGE);
		activity.setAppWaitActivity("com.facebook.*");
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		
		// Wait for an expected activity
		String contentPath = "//*[@resource-id='android:id/content']";
		String loginPath = contentPath + "/*[@class='android.widget.LinearLayout']/*[@class='android.widget.FrameLayout']/*[@class='android.widget.FrameLayout']/*[@class='android.widget.FrameLayout']/*[@class='android.view.ViewGroup']";
		String switchAccountPath = contentPath + "/*[@class='android.widget.FrameLayout']/*[@class='android.widget.LinearLayout']//*[@class='android.widget.Button' and string-length(@text) > 0]";
		FacebookActivityExpectedCondition activityCondition = new FacebookActivityExpectedCondition();
		activityCondition.setAppActivity(APP_ACTIVITY);
		activityCondition.setLoginActivity(LOGIN_ACTIVITY);
		activityCondition.setDeviceLoginActivity(DEVICE_LOGIN_ACTIVITY);
		activityCondition.setLoginPath(loginPath);
		activityCondition.setSwitchAccountPath(switchAccountPath);
		String current = wait.until(activityCondition);
		
		if (LOGIN_ACTIVITY.equals(current)) {
			// Simple login screen
			login(driver, loginPath);
		} else if (DEVICE_LOGIN_ACTIVITY.equals(current)) {
			// Device login screen, redirect to simple login screen
			driver.findElementByXPath(switchAccountPath).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(loginPath)));
			login(driver, loginPath);
		} else if (!APP_ACTIVITY.equals(current)) {
			LOGGER.info("Unexpected activity start: {}", current);
			result.setError(true);
			result.setCode("DST-FACEBOOK-005");
			result.setMessage(current);
			TestUtils.saveScreenshot(driver);
			return result;
		}
		
		/*
		 * Facebook main screen clicks:
		 * - "Home" button
		 * - "Photo" button
		 */
		String homeButtonPath = contentPath + "//*[@class='android.widget.LinearLayout' and count(./android.view.View) = 6]/*[@class='android.view.View']";

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(homeButtonPath))).click();
		String listPath = "//*[@resource-id='android:id/list']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
		// String pictureButtonPath = listPath + "//*[@class='android.view.ViewGroup' and @clickable='true' and string-length(@content-desc) = 0][1]";
		String inputOptionStatePath = listPath + "/*[@class='android.view.ViewGroup'][1]/*[@class='android.view.ViewGroup'][1]/*[@class='android.view.ViewGroup'][2]";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(inputOptionStatePath))).click();

		/**
		 * Facebook "What's on your mind"
		 * - Select to option: Photo/video
		 */
		String pictureOrVideoPaths = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup'][1]/*[@class='android.view.ViewGroup']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(pictureOrVideoPaths))).click();


		/*
		 * Facebook gallery clicks:
		 * - "Album" selector (if applies)
		 * - "Album" button (if applies)
		 * - First picture
		 * - "Next" button
		 */
		if (StringUtils.isNotEmpty(folderName)) {
			String folderSelectPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup']/*[@class='android.view.ViewGroup']/*[@class='android.view.ViewGroup'][2]/*[@class='android.view.ViewGroup']";
			//String folderSelectPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup'][1]/*[@class='android.view.ViewGroup'][2]";
			// String folderSelectPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup']//*[@class='android.view.ViewGroup'][0]";
			// Wait twice because of strange error
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(folderSelectPath)));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(folderSelectPath))).click();
			String paramSelectPath = contentPath + "//*[starts-with(@content-desc,'" + folderName + "')]";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(paramSelectPath))).click();
		}
		String firstPicturePath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup'][2]";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(firstPicturePath))).click();
		String nextPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.view.ViewGroup']//*[@class='android.view.ViewGroup'][3]";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(nextPath))).click();
		
		// Click "DONE" if photo goes to edit mode
		try {
			String editButtonsPath = contentPath + "//*[@class='android.widget.Button']/*[@class='android.widget.LinearLayout']/*[@class='android.widget.TextView' and string-length(@text) > 0]";
			WebDriverWait editButtonsWait = new WebDriverWait(driver, EDIT_BUTTONS_TIMEOUT);
			editButtonsWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(editButtonsPath)));
			List<AndroidElement> editButtons = driver.findElementsByXPath(editButtonsPath);
			if (!editButtons.isEmpty()) {
				editButtons.get(editButtons.size() - 1).click();
			}
		} catch (TimeoutException e) {
			// Not in edit mode, continue
		}
		
		/*
		 * Facebook publish clicks:
		 * - Comment text (click and fill)
		 * - "Publish" button
		 */
		String commentPath = contentPath + "//*[@class='android.widget.EditText' and @clickable='true']";
		// String publishPath = contentPath + "//*[@class='android.widget.Button' and @clickable='true']";
		String publishPath = "//*[@class='android.view.ViewGroup' and @clickable='true']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(commentPath))).click();
		new Actions(driver).sendKeys(comment).perform();
		driver.findElementByXPath(publishPath).click();
		long start = System.currentTimeMillis();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
		
		// Open notifications and click "Facebook"
		driver.openNotifications();
		
//		String notificationScrollPath = "//*[@resource-id='com.android.systemui:id/notification_stack_scroller']";
		// Dejaron de ser clicables los id/title e id/app_name_text
//		String notificationTitlePath = notificationScrollPath + "/*[@class='android.widget.FrameLayout']//*[@resource-id='android:id/title' or @resource-id='android:id/app_name_text']";
//		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(notificationScrollPath)));
//		
//		List<AndroidElement> titles = driver.findElementsByXPath(notificationTitlePath);
//		
//		if (!clickNotification(titles)) {
//			result.setError(true);
//			result.setCode("DST-FACEBOOK-006");
//			return result;
//		}
		
		// String notificationTitlePath = "//*[@class='android.widget.FrameLayout' and ./*[@id='expanded' and ./*[./*[./*[@id='icon'] and ./*[@text='Facebook']]]] and ./*[@id='backgroundNormal']]";
		String notificationTitlePath = "//*[@text='Subida de Facebook finalizada' or @text='Facebook upload complete']";
		//
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(notificationTitlePath))).click();
		
		
		// LOGGER.info("SE CLICKEO TITLE");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(contentPath)));
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		
		// Facebook publication progress screen (wait for "Uploading" to become "Upload Finished")
		String inProgressPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.widget.LinearLayout'][2]//*[starts-with(@text,'Subiendo') or starts-with(@text,'Uploading')]";
		String successPath = contentPath + "//*[@class='androidx.recyclerview.widget.RecyclerView']/*[@class='android.widget.LinearLayout'][2]//*[@text='Subida finalizada' or @text='Upload Finished']";
		WebDriverWait longWait = new WebDriverWait(driver, UPLOAD_TIMEOUT);
		longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(inProgressPath)));
		try {
			driver.findElementByXPath(successPath);
			result.setTime(System.currentTimeMillis() - start);
			result.setError(false);
		} catch (NoSuchElementException e) {
			LOGGER.error("Error in file upload", e);
			result.setError(true);
			result.setCode("DST-FACEBOOK-007");
			TestUtils.saveScreenshot(driver);
		}
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		return result;
	}


	private void login(AndroidDriver<AndroidElement> driver, String contentPath) {
		String loginEditPath = contentPath + "//*[@class='android.widget.EditText']";
		String loginButtonPath = contentPath + "//*[@content-desc='Iniciar sesión' or @content-desc='Log In']";
		List<AndroidElement> editTexts = driver.findElementsByXPath(loginEditPath);
		
		AndroidElement userEdit = editTexts.get(0);
		userEdit.click();
		new Actions(driver).sendKeys(username).perform();
		// Click outside to avoid auto complete
		Point userPoint = userEdit.getCenter();
		userPoint.move(0, userPoint.getY());
		new TouchAction<>(driver).press(new PointOption<>().withCoordinates(userPoint)).perform();
		
		AndroidElement passEdit = editTexts.get(1);
		passEdit.click();
		new Actions(driver).sendKeys(password).perform();
		// Click outside to avoid auto complete
		Point passPoint = passEdit.getCenter();
		passPoint.move(0, passPoint.getY());
		new TouchAction<>(driver).press(new PointOption<>().withCoordinates(passPoint)).perform();
		
		driver.findElementByXPath(loginButtonPath).click();
	}
	
	private boolean clickNotification(List<AndroidElement> titles) {
		for (AndroidElement t : titles) {
			if ("facebook".equalsIgnoreCase(t.getText())) {
				LOGGER.info("CLICKANDO: {} - {}", t, t.getText());
				t.click();
				return true;
			}
		}
		LOGGER.error("Error al clickar");
		return false;
	}

}
