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
import org.openqa.selenium.Point;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;

public class EntelPeruAppTestVerifyPostPaidAdditionalLine implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntelPeruAppTestVerifyPostPaidBalance.class);
    private static final String APP_PACKAGE = "com.entel.movil";
    private static final String APP_ACTIVITY = ".MainActivity";

    private static final int EDIT_BUTTONS_TIMEOUT = 10;

    private static final int UPLOAD_TIMEOUT = 300;

    private static final int COUNT_ELEMENTS_SLIDE = 1;

    private String phoneNumber;
    private String verificationCode;
    private TestResultEntelApp testResult;

    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEPS_TEST;

    static {
        STEPS_TEST = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-D007-E001", "Acceder a Línea adicional"));
    }


    public EntelPeruAppTestVerifyPostPaidAdditionalLine(String phoneNumber, String verificationCode) {
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
        verifyPostPaidAdditionalLine(driver, contentPath);
        return testResult;
    }


    public void verifyPostPaidAdditionalLine(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStep = STEPS_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();

        testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_ADDITIONAL_LINE_SUCCESS_ENTEL_PERU_APP.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_ADDITIONAL_LINE_SUCCESS_ENTEL_PERU_APP.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();
        try {
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_ENTEL_PERU);
            /*String homeTabPathSelected = contentPath + "//*[@resource-id='b4-b1-b1-BottomBarItems']/*[@class='android.view.View'][1]";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(homeTabPathSelected)))
                    .click();*/

            Dimension size = driver.manage().window().getSize();
            int y1 = (int) (size.height * 0.40);
            int x1 = size.width / 2;
            int x2 = size.width / 7;

            TouchAction ts = new TouchAction(driver);
            for (int i = 0; i < COUNT_ELEMENTS_SLIDE; i++) {
                ts.waitAction(WaitOptions.waitOptions(Duration.ofSeconds(4)))
                        .press(PointOption.point(x1, y1))
                        .moveTo(PointOption.point(x2, y1))
                        .release()
                        .perform();
            }
            /*String cardAdditionalLinePathSelected = contentPath + "//*[@resource-id='b1-b1-Content']//*[@resource-id='b1-b6-CarouselItems']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(cardAdditionalLinePathSelected)))
                    .click();*/

            //sampleTouch(driver);
            /*Dimension size = driver.manage().window().getSize();
            int y = (int) (size.height * 0.10);
            int x = size.width -100;

            Thread.sleep(5000);
            TouchAction ts = new TouchAction(driver);
            ts.tap(new PointOption<>().withCoordinates(new Point(x, y)))
                    .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                    .perform();*/
            Thread.sleep(6000);
            currentTimeOut = System.currentTimeMillis();
            String selectAdditionalLineTextPath = contentPath + "//*[@resource-id='b1-b1-MainContent']/*[@class='android.widget.TextView'][1]";
            String selectAdditionalLineTextLabel = wait//.withTimeout(Duration.ofSeconds(20))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(selectAdditionalLineTextPath)))
                    .getText();
            boolean expectedTextFound = selectAdditionalLineTextLabel.trim()
                    .equals("Obtenlo hoy por nuestros canales");
            String finalMessage = String.format("Texto \"Obtenlo hoy por nuestros canales\" %s fue encontrado en la pantalla seleccionada", (expectedTextFound ? "si" : "no"));
            testResult.setError(!expectedTextFound);
            if (testResult.isError()){
                testResult.setErrorMessage(finalMessage);
                testResult.setCode(EnumCodeProcessTests.VERIFY_RECEIPT_ERROR_ENTEL_PERU_APP.getCode());
            }
            TestUtils.saveScreenshot(driver);

            testResult.addItemDetail((new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D007-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentTimeOut)));
        } catch (Exception e) {

            testResult.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentTimeOut)
            );
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_ADDITIONAL_LINE_ERROR_ENTEL_PERU_APP.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_ADDITIONAL_LINE_ERROR_ENTEL_PERU_APP.getDescription());
            testResult.setErrorMessage(e.getMessage());

            TestUtils.saveScreenshot(driver);
        } finally {
            if (testResult.isError()){
                testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_ADDITIONAL_LINE_ERROR_ENTEL_PERU_APP.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeApp(driver);
        }
    }
}
