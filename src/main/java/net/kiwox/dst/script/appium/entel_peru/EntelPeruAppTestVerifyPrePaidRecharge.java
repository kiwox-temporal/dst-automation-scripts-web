package net.kiwox.dst.script.appium.entel_peru;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.DEFAULT_WAIT_TIMEOUT_ENTEL_PERU;
import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.closePromotionPopUp;

public class EntelPeruAppTestVerifyPrePaidRecharge implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntelPeruAppTestVerifyPostPaidBalance.class);
    private static final String APP_PACKAGE = "com.entel.movil";
    private static final String APP_ACTIVITY = ".MainActivity";

    private static final int EDIT_BUTTONS_TIMEOUT = 10;

    private static final int UPLOAD_TIMEOUT = 300;

    private String phoneNumber;
    private String verificationCode;

    public EntelPeruAppTestVerifyPrePaidRecharge(String phoneNumber, String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
    }

    @Override
    public TestResult runTest(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        TestResult result = new TestResult();

        // TODO: separte startup tests configurations
        // Start activity
        Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
        activity.setAppWaitPackage(APP_PACKAGE);
        activity.setAppWaitActivity("com.entel.*");
        driver.startActivity(activity);
        //WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());

        // Wait for an expected activity
        String contentPath = "//*[@resource-id='android:id/content']";
        TestResultEntelApp testResult;
        EntelPeruAppTestSignIn sigInTestObject = new EntelPeruAppTestSignIn(this.phoneNumber, this.verificationCode);
        new EntelPeruAppTestSignIn(phoneNumber, verificationCode)
                .automateStepsSignIn(driver, contentPath);

        //closePromotionPopUp(driver);
        testResult = verifyPaymentBag(driver, contentPath);
        return testResult;
    }

    public TestResultEntelApp verifyPaymentBag(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {
        TestResultEntelApp testResult = new TestResultEntelApp();
        testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_SUCCESS_ENTEL_PERU_APP.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_SUCCESS_ENTEL_PERU_APP.getDescription());
        try {
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_ENTEL_PERU);

            String postPaidOptionPath = contentPath + "//*[@resource-id='b4-b1-b1-BottomBarItems']/*[@class='android.view.View'][4]";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(postPaidOptionPath)))
                    .click();

            String selectBagOfBuyTextPath = contentPath + "//*[@resource-id='b1-b1-Tus_Ultimas_Compras']/*[@class='android.widget.TextView'][1]";
            String selectBagOfBuyTextLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selectBagOfBuyTextPath)))
                    .getText();

            boolean excpectedTextFound = selectBagOfBuyTextLabel.trim()
                    .equals("Selecciona la bolsa que desees comprar");
            System.out.println(String.format("Texto [Selecciona la bolsa que desees comprar] encontrado = %s", excpectedTextFound));
            if (!excpectedTextFound) {
                testResult.setMessage("Text [Selecciona la bolsa que desees comprar] not found in balance screen");
            }
        } catch (Exception e) {
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_APP.getCode());
            TestUtils.saveScreenshot(driver);
        } finally {
            return testResult;
        }
    }
}
