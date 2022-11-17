package net.kiwox.dst.script.chrome.testflows;

import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;

public class EntelWebVerifyHistoricReceiptsPostPaidTest implements IChromeTest {

    private String phoneNumber;
    private String passCode;

    private TestResultEntelApp testResult;

    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEP_SLA_01, STEP_SLA_02, STEP_SLA_03;

    static {
        STEP_SLA_01 = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D004-E001", "Hacer clic a menú Mis recibos"));
        STEP_SLA_02 = Collections.singletonMap("Step02", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D004-E002", "Hacer clic a menú Recibo"));
        STEP_SLA_03 = Collections.singletonMap("Step03", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D004-E003", "Hacer clic a Ver recibo"));
    }


    public EntelWebVerifyHistoricReceiptsPostPaidTest(String phoneNumber, String code) {
        this.phoneNumber = phoneNumber;
        this.passCode = code;
        this.testResult = new TestResultEntelApp();
    }

    @Override
    public TestResult runTest(ChromeHttpDriver driver) throws InterruptedException {

        testResult = new EntelWebAuthenticationTest(phoneNumber, passCode)
                .automateStepsAuthentication(driver);

        verifyHistoricReceiptsPostPaidWeb(driver);

        return testResult;
    }

    private void verifyHistoricReceiptsPostPaidWeb(ChromeHttpDriver driver) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStepOne = STEP_SLA_01.get("Step01");
        AbstractMap.SimpleEntry<String, String> errorStepTwo = STEP_SLA_02.get("Step02");
        AbstractMap.SimpleEntry<String, String> errorStepThree = STEP_SLA_03.get("Step03");

        String currentStepErrorCode = errorStepOne.getKey(), currentStepDetail = errorStepOne.getValue();

        testResult.setCode(EnumCodeProcessTests.VERIFY_HISTORIC_RECEIPTS_POST_PAID_SUCCESS_ENTEL_PERU_WEB.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_HISTORIC_RECEIPTS_POST_PAID_SUCCESS_ENTEL_PERU_WEB.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();
        try {
            WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());
            String rootReceiptsId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtMisRecibos_block_wtMenuItem']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(rootReceiptsId)))
                    .click();
            TestUtils.saveScreenshotWebApp(driver);
            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D004-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut));
            currentTimeOut = System.currentTimeMillis();
            Thread.sleep(1000);

            currentStepDetail = errorStepTwo.getValue();
            currentStepErrorCode = errorStepTwo.getKey();
            String receiptId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtMisRecibos_block_wtMenuSubItems_wt218']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(receiptId)))
                    .click();
            TestUtils.saveScreenshotWebApp(driver);
            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D004-002")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut));
            currentTimeOut = System.currentTimeMillis();

            currentStepDetail = errorStepThree.getValue();
            currentStepErrorCode = errorStepThree.getKey();
            String pathReceiptViewLink = "//div[@class = 'col-6 text-align-right']//span[1]";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(pathReceiptViewLink)))
                    .click();
            Thread.sleep(2000);
            //TestUtils.saveScreenshotWebApp(driver);
            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D004-003")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut));

            switchTab(driver, 1);
            Thread.sleep(5000);
            TestUtils.saveScreenshotWebApp(driver);


        } catch (Exception e) {
            testResult.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentTimeOut)
            );
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_HISTORIC_RECEIPTS_POST_PAID_ERROR_ENTEL_PERU_WEB.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_HISTORIC_RECEIPTS_POST_PAID_ERROR_ENTEL_PERU_WEB.getDescription());
            testResult.setErrorMessage(e.getMessage());
            TestUtils.saveScreenshotWebApp(driver);
        } finally {
            if (testResult.isError()) {
                testResult.setMessage(EnumCodeProcessTests.VERIFY_HISTORIC_RECEIPTS_POST_PAID_ERROR_ENTEL_PERU_WEB.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeWebApp(driver);
        }
    }

}
