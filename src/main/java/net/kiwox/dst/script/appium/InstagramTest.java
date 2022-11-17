package net.kiwox.dst.script.appium;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
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
import net.kiwox.dst.script.appium.instagram.InstagramImageExpectedCondition;
import net.kiwox.dst.script.pojo.TestResult;

public class InstagramTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InstagramTest.class);
	private static final String APP_PACKAGE = "com.instagram.android";
	private static final String APP_ACTIVITY = "com.instagram.mainactivity.LauncherActivity";
	private static final String SIGNED_OUT_ACTIVITY = "com.instagram.nux.activity.SignedOutFragmentActivity";
	private static final String MAIN_ACTIVITY = "com.instagram.mainactivity.MainActivity";
	private static final int UPLOAD_TIMEOUT = 300;
	
	private String username;
	private String password;
	private String folderName;
	private String comment;

	public InstagramTest(String username, String password, String folderName, String comment) {
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
		activity.setAppWaitActivity("com.instagram.*");
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		WebDriverWait longWait = new WebDriverWait(driver, UPLOAD_TIMEOUT);

		// Wait to load
		String containerPath = "//*[@resource-id='com.instagram.android:id/layout_container_main']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(containerPath)));
		
		String current = driver.currentActivity();
		if (SIGNED_OUT_ACTIVITY.equals(current)) {
			/*
			 * If the screen is not simple login, click one of these buttons to redirect
			 * - "Switch Accounts"
			 * - "Already hace an account? Log in"
			 */
			String loginComponentPath = containerPath + "//*[@resource-id='com.instagram.android:id/login_component']";
			try {
				driver.findElementByXPath(loginComponentPath);
			} catch (NoSuchElementException e) {
				String goToLoginPath = containerPath + "//*[@resource-id='com.instagram.android:id/log_in_button']";
				String switchAccountsPath = containerPath + "//*[@resource-id='com.instagram.android:id/regular_footer']/*[@resource-id='com.instagram.android:id/left_button']";
				if (!clickIfPresent(driver, switchAccountsPath)) {
					clickIfPresent(driver, goToLoginPath);
				}
			}
			
			// Simple login screen
			String userPath = loginComponentPath + "//*[@resource-id='com.instagram.android:id/login_username']";
			String passPath = loginComponentPath + "//*[@resource-id='com.instagram.android:id/password']";
			String submitPath = loginComponentPath + "//*[@resource-id='com.instagram.android:id/next_button']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(loginComponentPath)));
			
			AndroidElement user = driver.findElementByXPath(userPath);
			user.click();
			user.clear();
			new Actions(driver).sendKeys(username).perform();
			// Click outside to avoid auto complete
			Point userPoint = user.getCenter();
			userPoint.move(0, userPoint.getY());
			new TouchAction<>(driver).press(new PointOption<>().withCoordinates(userPoint)).perform();
			
			AndroidElement pass = driver.findElementByXPath(passPath);
			pass.click();
			new Actions(driver).sendKeys(password).perform();
			// Click outside to avoid auto complete
			Point passPoint = pass.getCenter();
			passPoint.move(0, passPoint.getY());
			new TouchAction<>(driver).press(new PointOption<>().withCoordinates(passPoint)).perform();
			
			driver.findElementByXPath(submitPath).click();
		} else if (!MAIN_ACTIVITY.equals(current)) {
			LOGGER.info("Unexpected activity start: {}", current);
			result.setError(true);
			result.setCode("DST-INSTAGRAM-005");
			result.setMessage(current);
			TestUtils.saveScreenshot(driver);
			return result;
		}
		
		// Instagram main screen, click "+" button
		// String tabBarPath = "//*[@resource-id='com.instagram.android:id/tab_bar']/*[@class='android.widget.FrameLayout'][3]";
		String tabBarPath = "//*[@resource-id='com.instagram.android:id/action_bar_left_button']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(tabBarPath))).click();
		
		String backPath = "//*[@resource-id='com.android.systemui:id/back']";//*[@resource-id='com.android.systemui:id/back']
		String volverPath = "//*[@contentDescription='Volver al inicio']";
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(volverPath)));
		
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		driver.navigate().back();
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(tabBarPath))).click();
		
		//CLICK en Publicar si existe
		String publishButton = "//*[@resource-id='com.instagram.android:id/overflow_menu_item']";
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(publishButton))).click();
		} catch(Exception e) {
			// No existe, boton antiguo, intento continuar
			LOGGER.info("No PUBLICAR button, try to go to gallery");
		}
		
		// Wait for gallery
		String galleryGridPath = "//*[@resource-id='com.instagram.android:id/media_picker_grid_view']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(galleryGridPath)));
		
		/*
		 * Instagram gallery clicks:
		 * - "Album" selector (if applies)
		 * - "Album" button (if applies)
		 * - First picture (click and hold)
		 * - Wait for image to preview
		 * - "Next" button
		 * - "Next" button (to skip image edit screen)
		 */
		if (StringUtils.isNotEmpty(folderName)) {
			String folderSelectPath = "//*[@resource-id='com.instagram.android:id/gallery_folder_menu_alt']";
			String folderListPath = "//*[@resource-id='com.instagram.android:id/action_sheet_row_text_view']";
			String paramFolderPath = "//*[@resource-id='com.instagram.android:id/action_sheet_row_text_view' and @text='" + folderName + "']";
			driver.findElementByXPath(folderSelectPath).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(folderListPath)));
			driver.findElementByXPath(paramFolderPath).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(galleryGridPath)));
		}
		String galleryPicturePath = galleryGridPath + "//*[@class='android.widget.CheckBox']";
		AndroidElement galleryPicture = driver.findElementByXPath(galleryPicturePath);
		new Actions(driver).clickAndHold(galleryPicture).pause(1000).release().perform();
		String imagePreviewPath = "//*[@resource-id='com.instagram.android:id/crop_image_view']";
		String nextPath = "//*[@resource-id='com.instagram.android:id/next_button_imageview']";
		longWait.until(new InstagramImageExpectedCondition(imagePreviewPath, nextPath)).click();
		String imageContainerPath = "//*[@resource-id='com.instagram.android:id/creation_image_container']";
		wait.until(new InstagramImageExpectedCondition(imageContainerPath, nextPath)).click();
		
		/*
		 * Instagram share screen clicks:
		 * - Comment text (click and fill)
		 * - "Share" button
		 */
		String commentPath = "//*[@resource-id='com.instagram.android:id/caption_text_view' and @class='android.widget.EditText']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(commentPath))).click();
		new Actions(driver).sendKeys(comment).perform();
		driver.findElementByXPath(nextPath).click();
		long start = System.currentTimeMillis();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(tabBarPath)));
		
		/*
		 * In the main screen check for the text that indicates progress until it disappears
		 * If the text changes it is considered an error in the upload
		 */
		String inProgressPath = "//*[@resource-id='com.instagram.android:id/row_pending_media_status_textview' and (starts-with(@text,'Finishing') or starts-with(@text,'Finalizando'))]";
		String pendingMessagePath = "//*[@resource-id='com.instagram.android:id/row_pending_media_status_textview']";
		longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(inProgressPath)));
		driver.findElementByXPath(tabBarPath); // Just checking if the main screen is still active
		AndroidElement pendingMessage = null;
		try {
			pendingMessage = driver.findElementByXPath(pendingMessagePath);
		} catch (NoSuchElementException e) {
			// no pending message found, all OK
		}
		
		if (pendingMessage == null) {
			result.setTime(System.currentTimeMillis() - start);
			result.setError(false);
		} else {
			String t = pendingMessage.getText();
			LOGGER.info("Expected pending message to disappear but it's showing: {}", t);
			result.setError(true);
			result.setCode("DST-INSTAGRAM-006");
			result.setMessage(t);
			TestUtils.saveScreenshot(driver);
		}
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		return result;
	}
	
	private boolean clickIfPresent(AndroidDriver<AndroidElement> driver, String path) {
		try {
			driver.findElementByXPath(path).click();
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}

}
