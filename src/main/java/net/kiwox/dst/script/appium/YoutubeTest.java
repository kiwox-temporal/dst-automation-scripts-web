package net.kiwox.dst.script.appium;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
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
import net.kiwox.dst.script.appium.custom.CustomActions;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.YoutubeTestResult;

public class YoutubeTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeTest.class);
	private static final String APP_PACKAGE = "com.google.android.youtube";
	private static final String APP_ACTIVITY = "com.google.android.youtube.app.honeycomb.Shell$HomeActivity";
	private static final String APP_WAIT_ACTIVITY = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity";
	private static final int VIDEO_TIMEOUT = 60;
	
	private static final String CONTENT_DESC_ATTR = "content-desc";
	
	private String[] videos;

	public YoutubeTest(String[] videos) {
		this.videos = videos;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();

		// Start activity
		Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
		activity.setAppWaitPackage(APP_PACKAGE);
		activity.setAppWaitActivity(APP_WAIT_ACTIVITY);
		driver.startActivity(activity);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		WebDriverWait longWait = new WebDriverWait(driver, VIDEO_TIMEOUT);
		
		List<YoutubeTestResult> results = new LinkedList<>();
		long start = System.currentTimeMillis();
		boolean errorInSomeTest = false;
		for (String video : videos) {
			try {
				/*
				 * Home screen clicks:
				 * - "Search" button (magnifier icon)
				 * - Fill search text (clicks automatically)
				 * - "Enter" in keyboard
				 */
				String layoutPath = "//*[@resource-id='com.google.android.youtube:id/appbar_layout']";
				String menuItemPath = layoutPath + "//*[@resource-id='com.google.android.youtube:id/menu_item_view']";
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(menuItemPath)));
				List<AndroidElement> menuItems = driver.findElementsByXPath(menuItemPath);
				menuItems.get(menuItems.size() - 1).click();
				String searchTextPath = layoutPath + "//*[@resource-id='com.google.android.youtube:id/search_edit_text']";
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(searchTextPath)));
				new Actions(driver).sendKeys(video).sendKeys("\n").perform();
				
				// Wait for results layout to appear and loading bar to disappear
				String resultsLayoutPath = "//*[@resource-id='com.google.android.youtube:id/watch_while_layout_coordinator_layout']";
				String resultsPath = resultsLayoutPath + "//*[@resource-id='com.google.android.youtube:id/results']";
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resultsPath)));
				String loadingPath = resultsLayoutPath + "//*[@resource-id='com.google.android.youtube:id/load_progress']";
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(loadingPath)));
				
				// Click first video (scroll if ads, channels or playlists appear)
				AndroidElement row = findRow(driver, resultsPath);
				if (row == null) {
					LOGGER.error("No video found for [{}]", video);
					/* result.setError(true);
					result.setCode("DST-YOUTUBE-005");
					result.setMessage(video);
					return result;*/
					YoutubeTestResult r = new YoutubeTestResult();
					r.setVideo(video);
					r.setTime(0);
					results.add(r);
					errorInSomeTest = true;
					continue;
				}
				row.click();
				long dt = System.currentTimeMillis();
				
				// Wait for video to appear and loading bar to disappear
				String videoLayoutPath = "//*[@resource-id='com.google.android.youtube:id/next_gen_watch_container_layout']";
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(videoLayoutPath)));
				String videoLoadingPath = videoLayoutPath + "//*[@resource-id='com.google.android.youtube:id/player_loading_view_thin']";
				longWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(videoLoadingPath)));
				dt = System.currentTimeMillis() - dt;
				
				/*
				 * Video screen clicks:
				 * - Device back button
				 * - "Close" button ("X" icon on bottom bar) 
				 */
				driver.pressKey(new KeyEvent(AndroidKey.BACK));
				String closeButtonPath = "//*[@resource-id='com.google.android.youtube:id/next_gen_watch_layout']//*[@resource-id='com.google.android.youtube:id/floaty_close_button']";
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(closeButtonPath))).click();
				
				// Go back to home screen
				String homeButtonPath = "//*[@resource-id='com.google.android.youtube:id/bottom_bar_container']//*[@class='android.widget.Button']";
				driver.findElementByXPath(homeButtonPath).click();
				
				YoutubeTestResult r = new YoutubeTestResult();
				r.setVideo(video);
				r.setTime(dt);
				results.add(r);
			} catch (Exception e) {
				LOGGER.error("Unexpected error during youtube test", e);
				TestUtils.saveScreenshot(driver);
				if (reset(driver, wait)) {
					LOGGER.info("Youtube test with video [{}] marked with error. Continues with the next one", video);
					YoutubeTestResult r = new YoutubeTestResult();
					r.setVideo(video);
					results.add(r);
					errorInSomeTest = true;
				} else {
					LOGGER.info("Couldn't reset youtube test screen. Finishing youtube test");
					driver.pressKey(new KeyEvent(AndroidKey.HOME));
					result.setError(true);
					result.setCode("DST-YOUTUBE-004");
					result.setMessage(e.getMessage());
					return result;
				}
			}
		}
		long time = System.currentTimeMillis() - start;
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		Gson gson = new Gson();
		result.setError(errorInSomeTest);
		result.setTime(time);
		result.setMessage(gson.toJson(results, List.class));
		return result;
	}
	
	private AndroidElement findRow(AndroidDriver<AndroidElement> driver, String resultsPath) {
		String anyPath = resultsPath + "/*[@focusable='true']";
		String rowPath = resultsPath + "//*[@class='android.view.ViewGroup' and string-length(@content-desc) > 0]";
		List<AndroidElement> elements;
		List<String> contentDescs = new LinkedList<>();
		List<String> classes = new LinkedList<>();
		boolean firstPage = true;
		
		while (!(elements = driver.findElementsByXPath(anyPath)).isEmpty()) {
			// Find video row
			List<AndroidElement> rows = driver.findElementsByXPath(rowPath);
			for (AndroidElement row : rows) {
				if (isVideoRow(row)) {
					return row;
				}
			}
			
			// No video found, scroll down
			if (firstPage) {
				// Save previous page data
				fill(elements, contentDescs, CONTENT_DESC_ATTR);
				fill(elements, classes, "class");
			} else {
				// Check if it did scroll by comparing with previous page data
				List<String> previousContentDescs = new LinkedList<>(contentDescs);
				List<String> previousClasses = new LinkedList<>(classes);
				fill(elements, contentDescs, CONTENT_DESC_ATTR);
				fill(elements, classes, "class");
				if (ListUtils.isEqualList(previousContentDescs, contentDescs)
						&& ListUtils.isEqualList(previousClasses, classes)) {
					return null;
				}
			}
			
			// Scroll down
			AndroidElement first = elements.get(0);
			AndroidElement last = elements.get(elements.size() - 1);
			new CustomActions(driver).dragAndDrop(last, first, 400).perform();
			
			firstPage = false;
		}
		return null;
	}
	
	private boolean isVideoRow(AndroidElement row) {
		String contentDesc = row.getAttribute(CONTENT_DESC_ATTR);
		if (contentDesc == null) {
			return false;
		}
		return contentDesc.endsWith("- play video")
				|| contentDesc.endsWith("- reproducir video");
	}
	
	private void fill(List<AndroidElement> elements, List<String> attributes, String attributeName) {
		attributes.clear();
		for (AndroidElement el : elements) {
			attributes.add(el.getAttribute(attributeName));
		}
	}
	
	private boolean reset(AndroidDriver<AndroidElement> driver, WebDriverWait wait) {
		String anyYoutubePath = "//*[starts-with(@resource-id, 'com.google.android.youtube')]";
		String homeButtonPath = "//*[@resource-id='com.google.android.youtube:id/bottom_bar_container']//*[@class='android.widget.Button']";
		do {
			try {
				driver.findElementByXPath(homeButtonPath).click();
				resetVideo(driver);
				return true;
			} catch (NoSuchElementException e) {
				// "Home" button not found, click back
			}

			driver.pressKey(new KeyEvent(AndroidKey.BACK));
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(homeButtonPath)));
			} catch (WebDriverException e) {
				// "Home" button still not found, try again
			}
		} while (!driver.findElementsByXPath(anyYoutubePath).isEmpty());
		return false;
	}
	
	private void resetVideo(AndroidDriver<AndroidElement> driver) {
		String closeButtonPath = "//*[@resource-id='com.google.android.youtube:id/next_gen_watch_layout']//*[@resource-id='com.google.android.youtube:id/floaty_close_button']";
		try {
			driver.findElementByXPath(closeButtonPath).click();
		} catch (NoSuchElementException e) {
			// Video is not running, do nothing
		}
	}

}
