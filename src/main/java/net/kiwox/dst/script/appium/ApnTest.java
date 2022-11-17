package net.kiwox.dst.script.appium;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.MobileElement;
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

public class ApnTest implements ITest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApnTest.class);
	private static final String APP_PACKAGE = "com.android.settings";
	private static final String APP_ACTIVITY = "com.android.settings.Settings$ApnSettingsActivity";
	private static final String APP_ACTIVITY8 = "com.android.settings.Settings";

	private static final String LIST_PATH = "//*[@resource-id='com.android.settings:id/list']";
	private static final String ROW_CLICKABLE_SUFFIX = "/*[@class='android.widget.LinearLayout' and @clickable='true']";
	private static final String APN_PATH = "//*[@text='Nombres de puntos de acceso']";
	private static final String MOBILE_NETWORK_PATH = "//*[@class='android.widget.RelativeLayout' and ./*[@text='Red móvil']]";
	
	private String apn;
	private String ping;

	public ApnTest(String apn, String ping) {
		this.apn = apn;
		this.ping = ping;
	}

	@Override
	public TestResult runTest(AndroidDriver<AndroidElement> driver) {
		TestResult result = new TestResult();
		
		// No APN, just ping
		if (apn == null) {
			if (ping(driver)) {
				result.setError(false);
			} else {
				result.setError(true);
				result.setCode("DST-SELAPN-007");
			}
			return result;
		}
		
		// Start activity
		Activity a = new Activity(APP_PACKAGE, APP_ACTIVITY);
		a.setOptionalIntentArguments("--ei sub_id 1");
		driver.startActivity(a);
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		
		// Wait to load
		String listPath = LIST_PATH;
		String rowPath = listPath + ROW_CLICKABLE_SUFFIX;
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
		}catch(Exception e) {
			try {
				LOGGER.info("Checking recycler container...");
				listPath = "//*[@resource-id='com.android.settings:id/recycler_view']";
				rowPath = listPath + ROW_CLICKABLE_SUFFIX;
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
			} catch (Exception e2) {
				try {
					// TELEFONO VIVO
					LOGGER.info("Buscando VIVO..");
					a = new Activity(APP_PACKAGE, "com.android.settings.Settings$VivoSimInfoActivity");
					driver.startActivity(a);
					wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
					
					scrollDown(driver, 1);
					
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(APN_PATH)));
					driver.findElement(By.xpath(APN_PATH)).click();
					
				}catch(Exception e4) {
					LOGGER.info("Checking list container...");
					
					a = new Activity(APP_PACKAGE, APP_ACTIVITY8);
					a.setOptionalIntentArguments("--ei sub_id 1");
					driver.startActivity(a);
					wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
					
					
					String listPath2 = "//*[@resource-id='com.android.settings:id/container_material']";
					try {
						driver.findElement(By.xpath("//*[@class='android.widget.LinearLayout' and ./*[@text='Internet y redes']]")).click();
						wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MOBILE_NETWORK_PATH)));
						driver.findElement(By.xpath(MOBILE_NETWORK_PATH)).click();
						wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Avanzada']")));
						driver.findElement(By.xpath("//*[@text='Avanzada']")).click();
						scrollDown(driver, 1);
						driver.findElement(By.xpath(APN_PATH)).click();
						
					}catch(Exception e3) {
						driver.findElement(By.xpath("//*[@class='android.widget.LinearLayout' and ./*[@text='Conexiones']]")).click();
						wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MOBILE_NETWORK_PATH)));
						driver.findElement(By.xpath("//*[@class='android.widget.RelativeLayout' and ./*[@text='Redes móviles']]")).click();
						wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='Nombres de punto de acceso']")));
						driver.findElement(By.xpath("//*[@text='Nombres de punto de acceso']")).click();
						listPath2 = LIST_PATH;
					}
					
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath2)));
					listPath = LIST_PATH;
					rowPath = listPath + ROW_CLICKABLE_SUFFIX;
				}
			}
		}
		scrollUp(driver, 1);
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		// scrollToTop(driver, rowPath);
		
		MobileElement apnRadioButton = findApnRadioButton(driver, rowPath);
		if (apnRadioButton == null) {
			scrollDown(driver, 1);
			apnRadioButton = findApnRadioButton(driver, rowPath);
		}
		if (apnRadioButton == null) {
			LOGGER.info("APN {} not found. Creating new...", apn);
			
			// Click button to add APN
			String addPath = "//*[@resource-id='com.android.settings:id/action_bar']//*[@class='android.widget.TextView' and @clickable='true']";
			try {
				driver.findElementByXPath(addPath).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
			}catch(Exception e) {
				LOGGER.info("Boton añadir no encontrado, se intenta por texto");
				addPath = "//*[@text='AÑADIR']";
				driver.findElementByXPath(addPath).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
			}
			
			List<AndroidElement> rows = driver.findElementsByXPath(rowPath);
			if (rows.size() < 2) {
				LOGGER.error("Elements to add new APN {} not found", apn);
				result.setError(true);
				result.setCode("DST-SELAPN-005");
				TestUtils.saveScreenshot(driver);
			} else {
				String editPath = "//*[@resource-id='android:id/edit']";
				String acceptPath = "//*[@resource-id='android:id/button1']";
//				String cancelPath = "//*[@resource-id='android:id/button1']";
				
				// Click NAME, type keyboard and press ACCEPT
				rows.get(0).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(editPath)));
				new Actions(driver).sendKeys(apn).perform();
				driver.findElementByXPath(acceptPath).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
				
				// Refresh rows
				rows = driver.findElementsByXPath(rowPath);
				
				// Click APN, type keyboard and press ACCEPT
				rows.get(1).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(editPath)));
				new Actions(driver).sendKeys(apn).perform();
				driver.findElementByXPath(acceptPath).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
				
//				int count = 0;
//				String titulo = "";
//				if(oldVersion) {// Refresh rows
//					scrollDown(driver, 1);
//					
//					// Click MCC and write 730, type keyboard and press ACCEPT
//					
//					while(!titulo.equalsIgnoreCase("MCC")||!titulo.equalsIgnoreCase("MCC")) {
//						count++;
//						rows = driver.findElementsByXPath(rowPath);
//						rows.get(count).click();
//						titulo = driver.findElement(By.xpath("//*[@resource-id='android:id/alertTitle']")).getText();
//						driver.findElementByXPath(cancelPath).click();
//					}
//					rows = driver.findElementsByXPath(rowPath);
//					rows.get(count).click();
//					// encontré el primero, el otro va siguiendo al primero
//					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(editPath)));
//					if(titulo.equalsIgnoreCase("MCC")) {
//						new Actions(driver).sendKeys("730").perform();
//					} else {
//						new Actions(driver).sendKeys("01").perform();
//					}
//					driver.findElementByXPath(acceptPath).click();
//					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
//					
//					// Refresh rows
//					rows = driver.findElementsByXPath(rowPath);
//					
//					count++;
//					rows.get(count).click();
//					titulo = driver.findElement(By.xpath("//*[@resource-id='android:id/alertTitle']")).getText();
//					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(editPath)));
//					// new Actions(driver).sendKeys("01").perform();
//					if(titulo.equalsIgnoreCase("MCC")) {
//						new Actions(driver).sendKeys("730").perform();
//					} else {
//						new Actions(driver).sendKeys("01").perform();
//					}
//					driver.findElementByXPath(acceptPath).click();
//					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
//				}
				
				// Press back button to save the APN
				driver.pressKey(new KeyEvent(AndroidKey.BACK));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(listPath)));
				
				scrollToTop(driver, rowPath);
				
				apnRadioButton = findApnRadioButton(driver, rowPath);
				
				if (apnRadioButton == null) {
					scrollDown(driver, 1);
					apnRadioButton = findApnRadioButton(driver, rowPath);
				}
				apnRadioButton.click();

				result.setCode("DST-SELAPN-006");
				LOGGER.info("New APN {} created successfully", apn);
			}
		} else {
			LOGGER.info("APN {} found. Clicking radio button", apn);
			apnRadioButton.click();
		}
		
		driver.pressKey(new KeyEvent(AndroidKey.HOME));
		
		if (ping(driver)) {
			result.setError(false);
		} else {
			result.setError(true);
			result.setCode("DST-SELAPN-007");
		}
		return result;
	}
	
	private void scrollDown(AndroidDriver<AndroidElement> driver, int repeatTimes) {
		Dimension size = driver.manage().window().getSize();
		TouchAction<?> action = new TouchAction<>(driver);
		int startX = size.width / 2;
		int startY = (int) (size.height * 0.8);
		int endY = (int) (size.width * 0.2);

		for (int i = 0; i < repeatTimes; i++) {
			action
				.press(PointOption.point(startX, startY))
				.waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
				.moveTo(PointOption.point(startX, endY))
				.release()
				.perform();
		}
	}
	
	private void scrollUp(AndroidDriver<AndroidElement> driver, int repeatTimes) {
		Dimension size = driver.manage().window().getSize();
		TouchAction<?> action = new TouchAction<>(driver);
		int startX = size.width / 2;
		int startY = (int) (size.height * 0.2);
		int endY = (int) (size.width * 0.8);

		for (int i = 0; i < repeatTimes; i++) {
			action
				.press(PointOption.point(startX, startY))
				.waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
				.moveTo(PointOption.point(startX, endY))
				.release()
				.perform();
		}
	}
	
	private void scrollToTop(AndroidDriver<AndroidElement> driver, String path) {
		List<AndroidElement> list;
		while((list = driver.findElementsByXPath(path)).size() > 1) {
			AndroidElement first = list.get(0);
			AndroidElement last = list.get(list.size() - 1);
			
			String firstText = findText(first);
			if (firstText == null) {
				return;
			}
			
			// Scroll up
			new CustomActions(driver).dragAndDrop(last, first, 400).perform();
			
			AndroidElement newFirst = driver.findElementByXPath(path);
			if (firstText.equals(findText(newFirst))) {
				// Found top
				return;
			}
		}
	}
	
	private MobileElement findApnRadioButton(AndroidDriver<AndroidElement> driver, String path) {
		List<String> rows = new LinkedList<>();
		List<AndroidElement> list;
		while (!(list = driver.findElementsByXPath(path)).isEmpty()) {
			rows.clear();
			
			// Check visible texts, return radio button if found
			for (AndroidElement el : list) {
				String text = findText(el);
				rows.add(text);
				try {
					MobileElement radio = el.findElementById("com.android.settings:id/apn_radiobutton");
					if (apn.equalsIgnoreCase(text)) {
						return radio;
					}
				} catch (NoSuchElementException e) {
					// not found, keep looking
				}
			}
			
			if (!scrollDown(driver, path, list, rows.get(0))) {
				return null;
			}
		}
		return null;
	}
	
	private boolean scrollDown(AndroidDriver<AndroidElement> driver, String path, List<AndroidElement> list, String firstText) {
		if (firstText == null) {
			return false;
		}
		
		int n = list.size();
		if (n < 2) {
			return false;
		} else {
			// Scroll down
			AndroidElement first = list.get(0);
			AndroidElement last = list.get(n - 1);
			new CustomActions(driver).dragAndDrop(last, first, 400).perform();
			
			// Check if it did scroll
			AndroidElement newFirst = driver.findElementByXPath(path);
			if (firstText.equals(findText(newFirst))) {
				return false;
			}
		}
		return true;
	}
	
	private String findText(AndroidElement el) {
		try {
			return el.findElementById("android:id/summary").getText();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	private boolean ping(AndroidDriver<AndroidElement> driver) {
		Map<String, Object> args = new HashMap<>();
		args.put("command", "ping");
		args.put("args", new String[] { "-c", "5", ping });
		args.put("includeStderr", true);
		Object out;
		try {
			out = driver.executeScript("mobile: shell", args);
		} catch (WebDriverException e) {
			String key = "StdErr:";
			if (e.getMessage().contains(key)) {
				int i = e.getMessage().indexOf(key) + key.length();
				int j = e.getMessage().indexOf('\n', i);
				if (j < 0) {
					throw e;
				}
				out = e.getMessage().substring(i, j);
			} else {
				throw e;
			}
		}
		
		if (out == null) {
			return false;
		}
		String output = out.toString().trim();
		return !output.equals("ping: unknown host " + ping) && !output.contains("100% packet loss");
	}

}
