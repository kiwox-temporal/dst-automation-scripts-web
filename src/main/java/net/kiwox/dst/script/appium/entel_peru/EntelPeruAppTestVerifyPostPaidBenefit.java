package net.kiwox.dst.script.appium.entel_peru;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;

public class EntelPeruAppTestVerifyPostPaidBenefit implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntelPeruAppTestVerifyPostPaidBalance.class);
    private static final String APP_PACKAGE = "com.entel.movil";
    private static final String APP_ACTIVITY = ".MainActivity";

    private static final int EDIT_BUTTONS_TIMEOUT = 10;

    private static final int UPLOAD_TIMEOUT = 300;

    private String phoneNumber;
    private String verificationCode;
    private TestResultEntelApp testResult;
    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEPS_TEST;
    static {
        STEPS_TEST = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-D005-E001", "Acceder a Beneficios"));
    }

    public EntelPeruAppTestVerifyPostPaidBenefit(String phoneNumber, String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.testResult = new TestResultEntelApp();
    }

    @Override
    public TestResult runTest(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        // TODO: separte startup tests configurations
        // Start activity
        Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
        activity.setAppWaitPackage(APP_PACKAGE);
        activity.setAppWaitActivity("com.entel.*");
        driver.startActivity(activity);
        //WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());

        // Wait for an expected activity
        String contentPath = "//*[@resource-id='android:id/content']";
        testResult = new EntelPeruAppTestSignIn(phoneNumber, verificationCode)
                .automateStepsSignIn(driver, contentPath);
        //closePromotionPopUp(driver);
        verifyPostPaidBenefit(driver, contentPath);
        return testResult;
    }

    public void verifyPostPaidBenefit(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStep = STEPS_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();


        testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BENEFIT_SUCCESS_ENTEL_PERU_APP.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BENEFIT_SUCCESS_ENTEL_PERU_APP.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();
        try {
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_ENTEL_PERU);
            String benefitTabPathSelected = contentPath + "//*[@resource-id='b4-b1-b1-BottomBarItems']/*[@class='android.view.View'][5]";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(benefitTabPathSelected)))
                    .click();

            currentTimeOut = System.currentTimeMillis();
            String firstItemBenefitPathSelected = contentPath + "//*[@resource-id='b1-Beneficios']/*[@class='android.view.View'][1]/*[@class='android.view.View'][1]";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(firstItemBenefitPathSelected)))
                    .click();

            String finalMessage = "Selecciona el TAB \"Bolsas\" dentro del menu principal y valida la existencia de beneficios.";
            if (testResult.isError()){
                testResult.setErrorMessage(finalMessage);
                testResult.setCode(EnumCodeProcessTests.VERIFY_RECEIPT_ERROR_ENTEL_PERU_APP.getCode());
            }
            TestUtils.saveScreenshot(driver);
            testResult.addItemDetail((new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D005-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut)));
        } catch (Exception e) {

            testResult.addItemDetail(addErrorDetailItem(
                    currentStepDetail,
                    currentStepErrorCode,
                    e.getMessage(),
                    currentTimeOut)
            );


            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BENEFIT_ERROR_ENTEL_PERU_APP.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BENEFIT_ERROR_ENTEL_PERU_APP.getDescription());
            testResult.setErrorMessage(e.getMessage());
            TestUtils.saveScreenshot(driver);
        } finally {
            if (testResult.isError()){
                testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BENEFIT_ERROR_ENTEL_PERU_APP.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeApp(driver);
        }
    }
}
