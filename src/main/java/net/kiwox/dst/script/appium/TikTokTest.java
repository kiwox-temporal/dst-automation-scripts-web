package net.kiwox.dst.script.appium;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
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
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import net.kiwox.dst.script.appium.custom.CustomActions;
import net.kiwox.dst.script.pojo.TestResult;

public class TikTokTest implements ITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(TikTokTest.class);
	private static final String APP_PACKAGE = "com.zhiliaoapp.musically";
	private static final String APP_ACTIVITY = "com.ss.android.ugc.aweme.main.MainActivity";
	private static final int SMALL_WAIT_TIMEOUT = 3;
	private static final int UPLOAD_TIMEOUT = 300;
	
	private String username;
	private String folderName;
	private String comment;

	public TikTokTest(String username, String folderName, String comment) {
		this.username = username;
		this.folderName = folderName;
		this.comment = comment;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		driver.configuratorSetWaitForIdleTimeout(Duration.ofSeconds(1));
		driver.configuratorSetWaitForSelectorTimeout(Duration.ofSeconds(1));
		
		TestResult result = new TestResult();
		
		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		WebDriverWait smallWait = new WebDriverWait(driver, SMALL_WAIT_TIMEOUT);
		
		/* Main Menu clicks:
		 * - Publish (but first wait for tutorials and other popups)
		 * - Upload
		 */
		waitAndClickPublishButton(driver, wait, smallWait);
		String uploadVideoPath = "//*[@resource-id='com.zhiliaoapp.musically:id/bbo']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(uploadVideoPath))).click();
		
		/* Gallery clicks:
		 * - Video tab (but first wait for popups)
		 * - Folder menu (and scroll to find folder)
		 * - Select first video
		 * - Wait for preview
		 * - Next
		 * - Next (again on edit video screen)
		 */
		waitAndClickVideoTab(driver, smallWait);
		if (StringUtils.isNotEmpty(folderName)) {
			String folderSelectPath = "//*[@resource-id='com.zhiliaoapp.musically:id/cah']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(folderSelectPath))).click();
			AndroidElement folder = findFolder(driver);
			if (folder == null) {
				LOGGER.info("Gallery not found: {}", folderName);
				result.setError(true);
				result.setCode("DST-TIKTOK-005");
				TestUtils.saveScreenshot(driver);
				return result;
			}
			folder.click();
		}
		String selectItemPath = "//*[@resource-id='com.zhiliaoapp.musically:id/bv0']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selectItemPath))).click();
		String previewPath = "//*[@resource-id='com.zhiliaoapp.musically:id/fxj']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(previewPath)));
		String nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/eu8']";
		driver.findElementByXPath(nextPath).click();
		nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/f_v' or @resource-id='com.zhiliaoapp.musically:id/d5a']";
		AndroidElement nextButton = (AndroidElement) wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(nextPath)));
		String nextId = nextButton.getAttribute("resource-id");
		nextButton.click();
		if ("com.zhiliaoapp.musically:id/f_v".equals(nextId)) {
			nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/d5a']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(nextPath))).click();
		}
		
		/* Upload video clicks:
		 * - Fill comment input
		 * - Click outside to hide keyboard
		 * - "Who can watch this video"
		 * - "Everyone"
		 * - Post
		 */
		String commentPath = "//*[@resource-id='com.zhiliaoapp.musically:id/b09']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(commentPath))).click();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String message = comment + ' ' + dateFormat.format(new Date());
		new Actions(driver).sendKeys(message).perform();
		String commentOutPath = "//*[@resource-id='com.zhiliaoapp.musically:id/ayz']";
		AndroidElement commentOut = driver.findElementByXPath(commentOutPath);
		Point commentOutPoint = commentOut.getCenter();
		commentOutPoint.move(0, commentOutPoint.getY());
		new TouchAction<>(driver).press(new PointOption<>().withCoordinates(commentOutPoint)).perform();
		String permissionPath = "//*[@resource-id='com.zhiliaoapp.musically:id/bre']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(permissionPath))).click();
		String allUsersPath = "//*[@resource-id='com.zhiliaoapp.musically:id/ddk']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(allUsersPath))).click();
		String postPath = "//*[@resource-id='com.zhiliaoapp.musically:id/dpy']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(postPath))).click();
		
		// Back to main screen (popups might appear before upload, so start counting again in that case)
		long start = System.currentTimeMillis();
		while (!waitAndClickProfileButton(driver, smallWait)) {
			start = System.currentTimeMillis();
		}
		
		/* Profile screen loop:
		 * - Click first video (wait until it appears)
		 * - Check username and comment
		 * - Click three dots
		 * - Scroll options to the end and click "Delete"
		 * - Click "Delete" on popup
		 * - Click back arrow (if available)
		 * - If the username or the comment don't match, repeat
		 */
		WebDriverWait longWait = new WebDriverWait(driver, UPLOAD_TIMEOUT);
		String uploadItemPath = "//*[@resource-id='com.zhiliaoapp.musically:id/am1']";
		String userPath = "//*[@resource-id='com.zhiliaoapp.musically:id/title']";
		String msgLabelPath = "//*[@resource-id='com.zhiliaoapp.musically:id/aq7']";
		String threeDotsPath = "//*[@resource-id='com.zhiliaoapp.musically:id/eeh']";
		String optBtnPath = "//*[@resource-id='com.zhiliaoapp.musically:id/eds']";
		String deletePath = "//*[@resource-id='android:id/button1']";
		String backPath = "//*[@resource-id='com.zhiliaoapp.musically:id/uk']";
		Long time = null;
		while (time == null) {
			longWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(uploadItemPath))).click();
			time = System.currentTimeMillis() - start;
			String user = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(userPath))).getText();
			String msgLabel = null;
			try {
				msgLabel = driver.findElementByXPath(msgLabelPath).getText();
			} catch (NoSuchElementException e) {
				// NO OP
			}
			
			if (user == null || !user.startsWith('@' + username) || !message.equals(msgLabel)) {
				time = null;
				result.setCode("DST-TIKTOK-006");
				LOGGER.warn("Username doesn't match [{}] or comment doesn't match [{}]. Time might not be accurate", username, message);
			}
			
			driver.findElementByXPath(threeDotsPath).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(optBtnPath)));
			List<AndroidElement> optBtns = driver.findElementsByXPath(optBtnPath);
			new CustomActions(driver).dragAndDrop(optBtns.get(optBtns.size() - 2), optBtns.get(0), 100).perform();
			optBtns = driver.findElementsByXPath(optBtnPath);
			new CustomActions(driver).dragAndDrop(optBtns.get(optBtns.size() - 2), optBtns.get(0), 100).perform();
			optBtns = driver.findElementsByXPath(optBtnPath);
			optBtns.get(optBtns.size() - 1).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(deletePath))).click();
			try {
				driver.findElementByXPath(backPath).click();
			} catch (NoSuchElementException | StaleElementReferenceException e) {
				// NO OP
			}
		}
		result.setTime(time);
		result.setError(false);
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		return result;
	}
	
	private AndroidElement findFolder(AndroidDriver<AndroidElement> driver) {
		List<AndroidElement> labels;
		List<String> names = new LinkedList<>();
		String labelPath = "//*[@resource-id='com.zhiliaoapp.musically:id/buw']";
		while (!(labels = driver.findElementsByXPath(labelPath)).isEmpty()) {
			names.clear();
			for (AndroidElement label : labels) {
				String name = label.getText();
				if (folderName.equals(name)) {
					return label;
				}
				names.add(name);
			}
			
			int n = labels.size();
			if (n < 2) {
				return null;
			}
			
			// Scroll down
			AndroidElement first = labels.get(0);
			AndroidElement last = labels.get(n - 1);
			new CustomActions(driver).dragAndDrop(last, first, 400).perform();
			
			// Check if it did scroll
			AndroidElement newFirst = driver.findElementByXPath(labelPath);
			if (names.get(0).equals(newFirst.getText())) {
				return null;
			}
		}
		return null;
	}
	
	private void waitAndClickPublishButton(AndroidDriver<AndroidElement> driver, WebDriverWait wait, WebDriverWait smallWait) {
		String publishPath = "//*[@resource-id='com.zhiliaoapp.musically:id/ctz']";
		TimeoutException timeout = null;
		int t = 0;
		while (t < TestUtils.getWaitTimeout()) {
			
			// Check for publish button
			WebElement publishButton = null;
			try {
				publishButton = smallWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(publishPath)));
			} catch (TimeoutException e) {
				timeout = e;
			}
			
			// Check for popups and tutorials (if not found click publish button if it's on screen)
			if (clickWhenWaitingForPublishButton(driver, wait)) {
				t = 0;
			} else if (publishButton == null) {
				t += SMALL_WAIT_TIMEOUT;
			} else {
				publishButton.click();
				return;
			}
		}
		if (timeout != null) {
			throw timeout;
		}
	}
	
	private boolean clickWhenWaitingForPublishButton(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		// Scroll down tutorial
		try {
			String scrollTutorialPath = "//*[@resource-id='com.zhiliaoapp.musically:id/cq7']";
			String titlePath = "//*[@resource-id='com.zhiliaoapp.musically:id/e2n']";
			AndroidElement tutorialOverlay = driver.findElementByXPath(scrollTutorialPath);
			AndroidElement title = driver.findElementByXPath(titlePath);
			Rectangle rect = tutorialOverlay.getRect();
			Point tutorialPos = new Point(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight());
			Point titlePos = title.getCenter();
			new TouchAction<>(driver)
				.press(new PointOption<>().withCoordinates(tutorialPos))
				.waitAction(new WaitOptions().withDuration(Duration.ofSeconds(1)))
				.moveTo(new PointOption<>().withCoordinates(titlePos))
				.release()
				.perform();
			return true;
		} catch (NoSuchElementException e) {
			// NO OP
		}
		
		// Accept terms popup
		try {
			String acceptTermsPath = "//*[@resource-id='com.zhiliaoapp.musically:id/a2h']";
			driver.findElementByXPath(acceptTermsPath).click();
			return true;
		} catch (NoSuchElementException e) {
			// NO OP
		}
		
		// Synchronize contacts popup
		try {
			String syncPopupPath = "//*[@resource-id='com.zhiliaoapp.musically:id/f7']//*[@class='android.widget.TextView'][2]";
			driver.findElementByXPath(syncPopupPath).click();
			return true;
		} catch (NoSuchElementException e) {
			// NO OP
		}
		
		// Configure topics tutorial
		try {
			String nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/eim']";
			driver.findElementByXPath(nextPath).click();
			nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/enz']";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(nextPath))).click();
			return true;
		} catch (NoSuchElementException e) {
			// NO OP
		}
		
		return false;
	}
	
	private void waitAndClickVideoTab(AndroidDriver<AndroidElement> driver, WebDriverWait smallWait) {
		String videoTabPath = "//*[@resource-id='com.zhiliaoapp.musically:id/e2n']";
		TimeoutException timeout = null;
		for (int t = 0; t < TestUtils.getWaitTimeout(); t += SMALL_WAIT_TIMEOUT) {
			
			// Check for video tab button
			WebElement videoTab = null;
			try {
				videoTab = smallWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(videoTabPath)));
			} catch (TimeoutException e) {
				timeout = e;
			}
			
			// Check for popup (if not found click video tab button if it's on screen)
			boolean popupClicked = false;
			try {
				String nextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/blh']";
				driver.findElementByXPath(nextPath).click();
				popupClicked = true;
			} catch (NoSuchElementException e) {
				// NO OP
			}
			
			if (!popupClicked && videoTab != null) {
				videoTab.click();
				return;
			}
		}
		if (timeout != null) {
			throw timeout;
		}
	}
	
	private boolean waitAndClickProfileButton(AndroidDriver<AndroidElement> driver, WebDriverWait smallWait) {
		String profilePath = "//*[@resource-id='com.zhiliaoapp.musically:id/cu4']";
		TimeoutException timeout = null;
		for (int t = 0; t < TestUtils.getWaitTimeout(); t += SMALL_WAIT_TIMEOUT) {
			
			// Check for profile button
			WebElement profileButton = null;
			try {
				profileButton = smallWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(profilePath)));
			} catch (TimeoutException e) {
				timeout = e;
			}
			
			// Check for popup (if not found return if profile button is on screen)
			try {
				String popupPath = "//*[@resource-id='com.zhiliaoapp.musically:id/f7']";
				driver.findElementByXPath(popupPath);
				String popupTextPath = "//*[@resource-id='com.zhiliaoapp.musically:id/aka']";
				List<AndroidElement> popupTexts = driver.findElementsByXPath(popupTextPath);
				String text = null;
				if (!popupTexts.isEmpty()) {
					text = popupTexts.get(0).getText();
				}
				
				if (text != null && text.contains("Facebook")) {
					/* Facebook contacts synchronization popup:
					 * - Video has been uploaded
					 * - Just click "No"
					 */
					String ignorePath = "//*[@resource-id='com.zhiliaoapp.musically:id/f7']//*[@class='android.widget.TextView'][2]";
					driver.findElementByXPath(ignorePath).click();
				} else {
					/* Private account popup (appears with new accounts):
					 * - Video has not been uploaded
					 * - Click "Post now"
					 * - Return false to restart time count
					 */
					String publishPath = "//*[@resource-id='com.zhiliaoapp.musically:id/f7']//*[@class='android.widget.TextView'][1]";
					driver.findElementByXPath(publishPath).click();
					return false;
				}
			} catch (NoSuchElementException e) {
				// NO OP
			}
			
			if (profileButton != null) {
				profileButton.click();
				return true;
			}
		}
		if (timeout != null) {
			throw timeout;
		}
		return false;
	}

}
