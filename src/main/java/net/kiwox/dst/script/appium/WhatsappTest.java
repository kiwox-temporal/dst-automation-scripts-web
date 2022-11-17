package net.kiwox.dst.script.appium;

import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import net.kiwox.dst.script.appium.custom.CustomActions;
import net.kiwox.dst.script.pojo.TestResult;

public class WhatsappTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WhatsappTest.class);
	private static final String APP_PACKAGE = "com.whatsapp";
	private static final String APP_ACTIVITY = ".HomeActivity";
	private static final int UPLOAD_TIMEOUT = 300;
	
	private String contactName;
	private String textToSend;
	private String galleryFolder;

	public WhatsappTest(String contactName, String textToSend) {
		this(contactName, textToSend, null);
	}

	public WhatsappTest(String contactName, String textToSend, String galleryFolder) {
		this.contactName = contactName;
		this.textToSend = textToSend;
		this.galleryFolder = galleryFolder;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();

		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		WebDriverWait longWait = new WebDriverWait(driver, UPLOAD_TIMEOUT);

		// Wait to load
		String contentPath = "//*[@resource-id='android:id/content']";
		String tabPath = contentPath + "//*[@resource-id='com.whatsapp:id/tab']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(tabPath))).click();
		
		// Find recent conversation with contact
//		String searchPath = "//*[@resource-id='com.whatsapp:id/header']//*[@resource-id='com.whatsapp:id/menuitem_search']";
//		String rowPath = "//*[@resource-id='android:id/result_list']//*[@resource-id='com.whatsapp:id/conversations_row_contact_name']";
//		// String rowPath = "//*[@id='conversations_row_contact_name' and ./parent::*[./parent::*[@id='conversations_row_header' and ./parent::*[./parent::*[./parent::*[@id='result_list']]]]]]";
//		AndroidElement row = findRow(driver, wait, searchPath, rowPath);
//		
//		// Recent conversation not found, create new chat
//		if (row == null) {
//			String newChatPath = contentPath + "//*[@resource-id='com.whatsapp:id/fab']";
//			driver.findElementByXPath(newChatPath).click();
//
//			searchPath = "//*[@resource-id='com.whatsapp:id/toolbar']//*[@resource-id='com.whatsapp:id/menuitem_search']";
//			rowPath = "//*[@resource-id='android:id/list']//*[@resource-id='com.whatsapp:id/contactpicker_row_name']";
//			row = findRow(driver, wait, searchPath, rowPath);
//		}
//		
//		if (row == null) {
//			LOGGER.info("Contact not found: {}", contactName);
//			result.setError(true);
//			result.setCode(getCode("005"));
//			return result;
//		}
//		row.click();
		
		String newChatPath = contentPath + "//*[@resource-id='com.whatsapp:id/fab']";
		driver.findElementByXPath(newChatPath).click();
		String searchPath = "//*[@resource-id='com.whatsapp:id/toolbar']//*[@resource-id='com.whatsapp:id/menuitem_search']";
		String rowPath = "//*[@resource-id='android:id/list']//*[@resource-id='com.whatsapp:id/contactpicker_row_name']";
		AndroidElement row = findRow(driver, wait, searchPath, rowPath);
		if (row == null) {
			LOGGER.info("Contact not found: {}", contactName);
			result.setError(true);
			result.setCode(getCode("005"));
			return result;
		}
			
		row.click();
		
		String listPath = "//*[@resource-id='com.whatsapp:id/conversation_layout']//*[@resource-id='android:id/list']";
		long start;
		if (galleryFolder == null) {
			sendText(driver, wait);
			start = System.currentTimeMillis();
		} else {
			if (sendPicture(driver, wait)) {
				start = System.currentTimeMillis();
				
				// Wait for conversation and scroll down just in case
				WebElement list = longWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
				new Actions(driver).dragAndDropBy(list, 0, - list.getRect().getHeight()).perform();
			} else {
				LOGGER.info("Gallery not found: {}", galleryFolder);
				result.setError(true);
				result.setCode(getCode("008"));
				return result;
			}
		}
		
		// Wait for clock icon to disappear
		String pendingPath = listPath + "//*[@resource-id='com.whatsapp:id/status' and (@content-desc='Esperando' or @content-desc='Pending')]";
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(pendingPath)));
		
		// Wait for progress bar to disappear
		String lastLayoutPath = listPath + "/*[@class='android.view.ViewGroup'][last()]";
		if (galleryFolder != null) {
			String progressPath = lastLayoutPath + "//*[@resource-id='com.whatsapp:id/media_container']//*[@resource-id='com.whatsapp:id/control_frame']";
			longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(progressPath)));
		}
		
		// Check that the last message in the conversation has the text and no clock icon
		long time = System.currentTimeMillis() - start;
		String lastMessagePath;
		String lastStatusPath;
		if (galleryFolder == null) {
			lastMessagePath = "//*[@resource-id='com.whatsapp:id/main_layout']//*[@resource-id='com.whatsapp:id/message_text']";
			lastStatusPath = "//*[@resource-id='com.whatsapp:id/main_layout']//*[@resource-id='com.whatsapp:id/status' and (@content-desc='Esperando' or @content-desc='Pending')]";
		} else {
			lastMessagePath = "//*[@resource-id='com.whatsapp:id/text_and_date']//*[@resource-id='com.whatsapp:id/caption']";
			lastStatusPath = "//*[@resource-id='com.whatsapp:id/text_and_date']//*[@resource-id='com.whatsapp:id/status' and (@content-desc='Esperando' or @content-desc='Pending')]";
		}
		AndroidElement lastLayout = driver.findElementByXPath(lastLayoutPath);
		MobileElement lastMessage = lastLayout.findElementByXPath(lastMessagePath);
		String msg = lastMessage.getText();
		if (!textToSend.equals(msg)) {
			LOGGER.info("Last message validation error. Expected [\"{}\"], got: [\"{}\"]", textToSend, msg);
			result.setError(true);
			result.setCode(getCode("006"));
			result.setMessage(msg);
			return result;
		}
		try {
			driver.findElementByXPath(lastStatusPath);
			LOGGER.info("Last message validation error. The pending icon was found.");
			result.setError(true);
			result.setCode(getCode("007"));
			return result;
		} catch (NoSuchElementException e) {
			// Not found is OK
		}

		String backPath = "//*[@resource-id='com.whatsapp:id/back']";
		driver.findElementByXPath(backPath).click();
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		result.setError(false);
		result.setTime(time);
		return result;
	}
	
	private AndroidElement findRow(AndroidDriver<AndroidElement> driver, WebDriverWait wait, String searchPath, String rowPath) {
		/*
		 * Clicks:
		 * - "Search" button (magnifier icon)
		 * - Wait and fill search text box
		 * - Device back button to hide keyboard
		 * - Return first row (or null if not found or not equals)
		 */
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(searchPath))).click();
		String searchTextPath = "//*[@resource-id='com.whatsapp:id/search_holder']//*[@resource-id='com.whatsapp:id/search_src_text']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(searchTextPath)));
		new Actions(driver).sendKeys(contactName).perform();
		driver.pressKey(new KeyEvent(AndroidKey.BACK));
		LOGGER.info("BUSCANDO ROW");
		try {
			AndroidElement row = driver.findElementByXPath(rowPath);
			LOGGER.info("ROW TEXT {}", row);
			if (contactName.equals(row.getText())) {
				return row;
			}
		} catch (NoSuchElementException e) {
			// Not found, return null
			LOGGER.warn("Contact not found {}", e.getMessage());
		}
		return null;
	}
	
	private void sendText(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		/*
		 * Conversation clicks:
		 * - Text to send (click and fill)
		 * - "Send" button (arrow icon)
		 */
		String messagePath = "//*[@resource-id='com.whatsapp:id/entry']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(messagePath))).click();
		new Actions(driver).sendKeys(textToSend).perform();
		String sendPath = "//*[@resource-id='com.whatsapp:id/send']";
		driver.findElementByXPath(sendPath).click();
	}
	
	private boolean sendPicture(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		/*
		 * Conversation clicks:
		 * - "Attach" button (clip icon)
		 * - "Gallery" button
		 */
		String attachPath = "//*[@resource-id='com.whatsapp:id/input_attach_button']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(attachPath))).click();
		String galleryPath = "//*[@resource-id='com.whatsapp:id/pickfiletype_gallery_holder']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(galleryPath))).click();
		
		// Find gallery
		String albumsPath = "//*[@resource-id='com.whatsapp:id/albums']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(albumsPath)));
		String framesPath = albumsPath + "/*[@class='android.widget.FrameLayout']";
		AndroidElement gallery = findGallery(driver, framesPath);
		if (gallery == null) {
			return false;
		}
		gallery.click();
		
		/*
		 * Gallery clicks:
		 * - Click first picture (and wait to load)
		 * - Text to send (click and fill)
		 * - "Send" button (arrow icon)
		 */
		String gridPath = "//*[@resource-id='com.whatsapp:id/grid']/*[@class='android.widget.ImageView']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(gridPath))).click();
		String photoPath = "//*[@resource-id='com.whatsapp:id/photo']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(photoPath)));
		String captionPath = "//*[@resource-id='com.whatsapp:id/caption']";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(captionPath))).click();
		new Actions(driver).sendKeys(textToSend).perform();
		String sendPath = "//*[@resource-id='com.whatsapp:id/send']";
		driver.findElementByXPath(sendPath).click();
		return true;
	}
	
	private AndroidElement findGallery(AndroidDriver<AndroidElement> driver, String path) {
		List<AndroidElement> frames;
		List<String> names = new LinkedList<>();
		while (!(frames = driver.findElementsByXPath(path)).isEmpty()) {
			names.clear();
			for (AndroidElement frame : frames) {
				String name = findText(frame);
				if (galleryFolder.equals(name)) {
					return frame;
				}
				names.add(name);
			}
			
			int n = frames.size();
			if (n < 2) {
				return null;
			} else {
				// Scroll down
				AndroidElement first = frames.get(0);
				AndroidElement last = frames.get(n - 1);
				new CustomActions(driver).dragAndDrop(last, first, 400).perform();
				
				// Check if it did scroll
				AndroidElement newFirst = driver.findElementByXPath(path);
				if (names.get(0).equals(newFirst.getText())) {
					return null;
				}
			}
		}
		return null;
	}
	
	private String findText(AndroidElement frame) {
		String titlePath = "//*[@resource-id='com.whatsapp:id/title']";
		try {
			return frame.findElementByXPath(titlePath).getText();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	private String getCode(String suffix) {
		String type = galleryFolder == null ? "WATEXT" : "WAPHOTO";
		return "DST-" + type + "-" + suffix;
	}

}
