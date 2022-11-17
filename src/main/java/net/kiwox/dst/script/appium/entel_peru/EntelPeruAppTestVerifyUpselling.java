package net.kiwox.dst.script.appium.entel_peru;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;

public class EntelPeruAppTestVerifyUpselling implements ITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntelPeruAppTestVerifyUpselling.class);
    private static final String APP_PACKAGE = "com.entel.movil";
    private static final String APP_ACTIVITY = ".MainActivity";

    private static final String LOGIN_ACTIVITY = "com.facebook.account.login.activity.SimpleLoginActivity";
    private static final String DEVICE_LOGIN_ACTIVITY = ".dbl.activity.DeviceBasedLoginActivity";

    private static final int EDIT_BUTTONS_TIMEOUT = 10;

    private static final int UPLOAD_TIMEOUT = 300;
    private static final int COUNT_ELEMENTS_SLIDE = 2;


    private String phoneNumber;
    private String verificationCode;

    private TestResultEntelApp testResult;
    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEPS_TEST;
    static {
        STEPS_TEST = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-D008-E001", "Acceder a Upselling"));
    }

    public EntelPeruAppTestVerifyUpselling(String phoneNumber, String verificationCode) {
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

        String contentPath = "//*[@resource-id='android:id/content']";
        testResult = new EntelPeruAppTestSignIn(phoneNumber, verificationCode)
                .automateStepsSignIn(driver, contentPath);

        //closePromotionPopUp(driver);
        verifyUpselling(driver, contentPath);
        return testResult;
    }

    public void verifyUpselling(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {

        AbstractMap.SimpleEntry<String, String> errorStep = STEPS_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();

        testResult.setCode(EnumCodeProcessTests.VERIFY_UPSELLING_SUCCESS_ENTEL_PERU_APP.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_UPSELLING_SUCCESS_ENTEL_PERU_APP.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();
        try {
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_ENTEL_PERU);
            // home tab selected by default
            String promotionSlider = contentPath + "//*[@resource-id='b1-PromotionMain']";

            // TODO: MODIFIED DEFAULT ERRROR
            Thread.sleep(1000);
            TestUtils.saveScreenshot(driver);
            testResult.addItemDetail((new TestResultDetailEntelApp().setCode(currentStepErrorCode)
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(true)
                    .setTime(System.currentTimeMillis() - currentTimeOut)));

            testResult.setCode(EnumCodeProcessTests.VERIFY_UPSELLING_ERROR_ENTEL_PERU_APP.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_UPSELLING_ERROR_ENTEL_PERU_APP.getDescription());
            TestUtils.saveScreenshot(driver);

        } catch (Exception e) {

            testResult.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    e.getMessage(),
                    currentStepDetail,
                    currentTimeOut)
            );
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_UPSELLING_ERROR_ENTEL_PERU_APP.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_UPSELLING_ERROR_ENTEL_PERU_APP.getDescription());
            testResult.setErrorMessage(e.getMessage());

            TestUtils.saveScreenshot(driver);
        } finally {
            if (testResult.isError()){
                testResult.setMessage(EnumCodeProcessTests.VERIFY_UPSELLING_ERROR_ENTEL_PERU_APP.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeApp(driver);
        }
    }


}
