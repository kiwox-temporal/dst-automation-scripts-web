package net.kiwox.dst.script.chrome.testflows;

import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.appium.entel_peru.EntelPeruAppTestSignIn;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.custom.MultipleExpectedCondition;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.addErrorDetailItem;
import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.closeWebApp;

public class EntelWebVerifyPostPaidBalanceTest implements IChromeTest {

    private String phoneNumber;
    private String passCode;

    private TestResultEntelApp testResult;

    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEP_SLA_01;
    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEP_SLA_02;

    static {
        STEP_SLA_01 = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D001-E001", "Hacer clic a menú Mis Saldos"));
        STEP_SLA_02 = Collections.singletonMap("Step02", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D001-E002", "Hacer clic en Saldos"));
    }


    public EntelWebVerifyPostPaidBalanceTest(String phoneNumber, String code) {
        this.phoneNumber = phoneNumber;
        this.passCode = code;
        this.testResult = new TestResultEntelApp();
    }

    @Override
    public TestResult runTest(ChromeHttpDriver driver) throws InterruptedException {

        testResult = new EntelWebAuthenticationTest(phoneNumber, passCode)
                .automateStepsAuthentication(driver);

        verifyPostPaidBalanceWeb(driver);

        return testResult;
    }

    private void verifyPostPaidBalanceWeb(ChromeHttpDriver driver) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStepOne = STEP_SLA_01.get("Step01");
        AbstractMap.SimpleEntry<String, String> errorStepTwo = STEP_SLA_02.get("Step02");

        String currentStepErrorCode = errorStepOne.getKey(), currentStepDetail = errorStepOne.getValue();

        testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_SUCCESS_ENTEL_PERU_WEB.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_SUCCESS_ENTEL_PERU_WEB.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();
        try {
            WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
            String rootBalanceId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtMisSaldos_block_wtMenuItem']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(rootBalanceId)))
                    .click();
            TestUtils.saveScreenshotWebApp(driver);
            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D001-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut));
            currentTimeOut = System.currentTimeMillis();
            Thread.sleep(1000);

            String balanceId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtMisSaldos_block_wtMenuSubItems_wt188']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(balanceId)))
                    .click();
            TestUtils.saveScreenshotWebApp(driver);
            String pathText = "//*[@id='PE_Web_Personas_TH_wt6_block_wtMainContent_PE_Web_Personas_CW_Common_wt14_block_wtInfoDate']";

            Thread.sleep(2000);
            String balanceText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(pathText)))
                    .getText();
                    //.getAttribute("innerHTML");
            TestUtils.saveScreenshotWebApp(driver);
            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D002-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut));
            //currentTimeOut = System.currentTimeMillis();

            boolean expectedTextFound = balanceText.trim()
                    .contains("Actualizado el");
            String finalMessage = String.format("Texto \"Actualizado el\" %s fue encontrado en la pantalla seleccionada", (expectedTextFound ? "si" : "no"));
            testResult.setError(!expectedTextFound);
            if (testResult.isError()) {
                testResult.setErrorMessage(finalMessage);
                testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_WEB.getCode());
            }
            TestUtils.saveScreenshot(driver);
        } catch (Exception e) {
            testResult.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentTimeOut)
            );
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_WEB.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_WEB.getDescription());
            testResult.setErrorMessage(e.getMessage());
            TestUtils.saveScreenshotWebApp(driver);
        } finally {
            if (testResult.isError()) {
                testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_WEB.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeWebApp(driver);
        }
    }

}
