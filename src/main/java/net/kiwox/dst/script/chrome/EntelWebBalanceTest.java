package net.kiwox.dst.script.chrome;

import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.chrome.custom.MultipleExpectedCondition;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.pojo.TestResult;

public class EntelWebBalanceTest implements IChromeTest {

	private String phoneNumber;
	private String passCode;

	public EntelWebBalanceTest(String phoneNumber, String code) {
		this.phoneNumber = phoneNumber;
		this.passCode = code;
	}

	@Override
	public TestResult runTest(ChromeHttpDriver driver) {
		long time = System.currentTimeMillis();
		driver.navigate().to("https://miperfil.entel.pe/Login");
		WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
		
		String phoneInputId = "PE_Web_Login_Per_TH_wt38_block_wtMainContent_wt14_PE_Web_Login_Per_PATT_wtHomeAccessBlock_block_wtContent_PE_Web_Login_Per_PATT_wt65_block_wtInput_wtNumeroDesktop";
		String phoneSubmitId = "PE_Web_Login_Per_TH_wt38_block_wtMainContent_wt14_PE_Web_Login_Per_PATT_wtHomeAccessBlock_block_wtContent_PE_Web_Login_Per_PATT_wt65_block_wtButton_wtConfirmIngresoMovil";
		WebElement phoneInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(phoneInputId)));
		phoneInput.click();
		phoneInput.sendKeys(phoneNumber);
		new Actions(driver).moveToElement(phoneInput).moveByOffset(phoneInput.getSize().getWidth(), 0).click().perform();
		wait.until(ExpectedConditions.elementToBeClickable(By.id(phoneSubmitId))).click();
		
		String companyInputId = "PE_Web_Login_B2B_TH_wt22_block_wtMainContent_wt21_PE_Web_Login_B2B_PATT_wt106_block_wtContent_PE_Web_Login_B2B_PATT_wt8_block_wtInput_wtNumeroDesktop";
		String codeInputId = "PE_Web_Login_B2B_TH_wt2_block_wtMainContent_wt11_PE_Web_Login_B2B_PATT_wt41_block_wtContent_PE_Web_Login_B2B_PATT_wtInputs_block_wtInputs_wtFirst";
		MultipleExpectedCondition loginCond = new MultipleExpectedCondition(By.id(companyInputId), By.id(codeInputId));
		Entry<Integer, WebElement> loginElement = wait.until(loginCond);
		WebElement codeInput;
		if (loginElement.getKey() == 0) {
			WebElement companyInput = loginElement.getValue();
			companyInput.click();
			companyInput.sendKeys(phoneNumber);
			new Actions(driver).moveToElement(companyInput).moveByOffset(companyInput.getSize().getWidth(), 0).click().perform();
			
			String companySubmitId = "PE_Web_Login_B2B_TH_wt22_block_wtMainContent_wt21_PE_Web_Login_B2B_PATT_wt106_block_wtContent_PE_Web_Login_B2B_PATT_wt8_block_wtButton_wtConfirmIngresoMovil";
			wait.until(ExpectedConditions.elementToBeClickable(By.id(companySubmitId))).click();
			codeInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(codeInputId)));
		} else {
			codeInput = loginElement.getValue();
		}
		
		codeInput.click();
		for (int i = 0; i < 6; ++i) {
			new Actions(driver).sendKeys(passCode.substring(i, i+1)).perform();
		}
		
		String balanceId = "PE_Web_B2B_TH_wt2_block_wtMenu_wt10_RichWidgets_wtSaldo2_block_wtMenuItem_wt10";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(balanceId))).click();
		balanceId = "PE_Web_B2B_TH_wt2_block_wtMenu_wt10_RichWidgets_wtSaldo2_block_wtMenuSubItems_wt534";
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(balanceId))).click();
		
		long balanceTime = System.currentTimeMillis();
		WebDriverWait longWait = new WebDriverWait(driver, 120);
		TestResult result = new TestResult();
		String balanceContainerId = "PE_Web_B2B_TH_wt14_block_wtMainContent_PE_Web_Entel_B2B_CW_Common_wt11_block_wtSaldoIlimitado";
		try {
			longWait.until(ExpectedConditions.presenceOfElementLocated(By.id(balanceContainerId))); // TODO: get id
		} catch (TimeoutException e) {
			result.setError(true);
			result.setCode("DST-ENTELWEBBALANCE-005");
		}
		balanceTime = System.currentTimeMillis() - balanceTime;
		time = System.currentTimeMillis() - time;
		
		result.setTime(time);
		result.setMessage("" + balanceTime);
		return result;
	}

}
