package net.kiwox.dst.script.appium.entel_peru;


import net.kiwox.dst.script.appium.ITest;
import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.aspectj.weaver.ast.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.touch.offset.PointOption;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;


public class EntelPeruAppTestSignIn implements ITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntelPeruAppTestSignIn.class);

    private static final String APP_PACKAGE = "com.entel.movil";
    private static final String APP_ACTIVITY = ".MainActivity";
    private static final int EDIT_BUTTONS_TIMEOUT = 10;
    private static final int UPLOAD_TIMEOUT = 300;

    private String phoneNumber;
    private String verificationCode;
    private Integer popupCloseX;
    private Integer popupCloseY;


    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEPS_TEST;

    static {
        STEPS_TEST = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-D000-E001", "Ingresar código SMS y Cerrar Promoción"));
    }
    //private TestResultEntelApp testResult;

    public EntelPeruAppTestSignIn(String phoneNumber, String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
    }

    public EntelPeruAppTestSignIn(String phoneNumber, String verificationCode, Integer popupCloseX, Integer popupCloseY) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.popupCloseX = popupCloseX;
        this.popupCloseY = popupCloseY;
    }

    @Override
    public TestResultEntelApp runTest(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        // Start activity
        Activity activity = new Activity(APP_PACKAGE, APP_ACTIVITY);
        activity.setAppWaitPackage(APP_PACKAGE);
        activity.setAppWaitActivity("com.entel.*");
        driver.startActivity(activity);
        String contentPath = "//*[@resource-id='android:id/content']";
        //TestResultEntelApp testResult;
        /*Map<String, Object> mapDataSignIn = userIsAuthenticated(driver, contentPath);
        if ((boolean) mapDataSignIn.get("auth")) {
            testResult = signInApp(driver, contentPath);
        } else {
            testResult = TestResultEntelApp.castEnumProcessToTestResult(EnumCodeProcessTests.SIGN_IN_SUCCESS_ENTEL_PERU_APP);
            testResult.details.add(new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D000-A001")
                    .setDescription("Usuario anteriormente logueado")
                    .setDetail("El flujo de logueo fue omitido por que el usuario ya se encontraba logueado")
                    .setErrorDetected(false)
                    .setTime((long) mapDataSignIn.get("time")));

            testResult.setTime((long) mapDataSignIn.get("time"));
            testResult.setMessage("Skip Automation Test: SignIn");

        }*/
        TestResultEntelApp testResult = automateStepsSignIn(driver, contentPath);
        closePromotionPopUp(driver, popupCloseX, popupCloseY);
        //closeAppSignIn(driver);
        return testResult;
    }

    public TestResultEntelApp automateStepsSignIn(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStep = STEPS_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();
        Map<String, Object> mapDataSignIn = userIsAuthenticated(driver, contentPath);
        TestResultEntelApp testResult = new TestResultEntelApp();
        testResult.setCode(EnumCodeProcessTests.SIGN_IN_SUCCESS_ENTEL_PERU_APP.getCode());
        testResult.setMessage(EnumCodeProcessTests.SIGN_IN_SUCCESS_ENTEL_PERU_APP.getDescription());

        TestResultDetailEntelApp openAppDetail = (TestResultDetailEntelApp) mapDataSignIn.get("openAppDetail");
        testResult.details.add(openAppDetail);


        if (((int) mapDataSignIn.get("auth")) == 0) {
            testResult = signInApp(driver, contentPath);
        } else {
            TestUtils.saveScreenshot(driver);
            testResult.details.add(new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D000-002")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime((long) mapDataSignIn.get("time")));
            testResult.setTime((long) mapDataSignIn.get("time"));
        }
        testResult.addTime(openAppDetail.getTime());
        return testResult;
    }

    public static Map<String, Object> userIsAuthenticated(AndroidDriver<AndroidElement> driver, String contentPath) {

        TestResultDetailEntelApp openAppDetail = addDetailOpenApp();
        Map<String, Object> map = new HashMap<>();
        long starTime = System.currentTimeMillis();
        //driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_SING_IN_ENTEL_PERU);
        String webViewLoginPath = contentPath + "//*[@class='android.webkit.WebView' and @text='LoginAWS']";
        int authenticated = 0;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(webViewLoginPath)));
        } catch (Exception e) {
            authenticated = 1;
        } finally {
            map.put("openAppDetail", openAppDetail);
            map.put("time", (System.currentTimeMillis() - starTime));
            map.put("auth", authenticated);
            return map;
        }
    }

    public TestResultEntelApp signInApp(AndroidDriver<AndroidElement> driver, String contentPath) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStep = STEPS_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();

        TestResultEntelApp result = new TestResultEntelApp();
        List<TestResultDetailEntelApp> details = new ArrayList<>();

        result.setCode(EnumCodeProcessTests.SIGN_IN_SUCCESS_ENTEL_PERU_APP.getCode());
        result.setMessage(EnumCodeProcessTests.SIGN_IN_SUCCESS_ENTEL_PERU_APP.getDescription());

        long initStartTimeGlobalTest = System.currentTimeMillis(), currentStarTimeTaskExecute = System.currentTimeMillis();
        try {
            TestUtils.saveScreenshot(driver);
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_SING_IN_ENTEL_PERU);
            String phoneNumberEditPath = contentPath + "//*[@resource-id='b1-b2-InputWrapper']";
            //String viewTagsCellsEditPath = contentPath + "//*[@class='android.view.View' and count(./android.widget.TextView) = 6]";
            //String loginButtonPath = contentPath + "//*[@class='android.widget.Button' and @text='CONTINUAR']";

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(phoneNumberEditPath)))
                    .click();

            //List<AndroidElement> editTexts = driver.findElementsByXPath(phoneNumberEditPath);
            //List<AndroidElement> tagsTexts = driver.findElementsByXPath(viewTagsCellsEditPath);
            new Actions(driver).sendKeys(this.phoneNumber)
                    .perform();

            // Click outside to avoid auto complete
            AndroidElement phoneNumberEdit = driver.findElementsByXPath(phoneNumberEditPath).get(0);
            Point phoneNumberPoint = phoneNumberEdit.getCenter();
            phoneNumberPoint.move(0, phoneNumberPoint.getY());
            new TouchAction<>(driver).press(new PointOption<>().withCoordinates(phoneNumberPoint)).perform();

            WebDriverWait editButtonsWait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_ENTEL_PERU);
            String loginButtonPath2 = contentPath + "//*[@class='android.view.View' and ./*[@text='CONTINUAR']]/*[@class='android.widget.Button'][1]";
            editButtonsWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(loginButtonPath2)))
                    .click();

            //AndroidElement verificationCodeTags = driver.findElementByXPath(verificationCodeEditPath);
            String verificationCodeEditPath = contentPath + "//*[@resource-id='b1-b4-Input_PhoneNumberVar']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(verificationCodeEditPath)))
                    .click();
            TestUtils.saveScreenshot(driver);
            //Thread.sleep(3000);
            new Actions(driver).sendKeys(this.verificationCode)
                    //.pause(1000)
                    .perform();

            details.add(new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D000-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(System.currentTimeMillis() - currentStarTimeTaskExecute));

            result.setDetails(details);
        } catch (Exception e) {

            result.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentStarTimeTaskExecute)
            );

            result.setError(true);
            result.setCode(EnumCodeProcessTests.SIGN_IN_ERROR_ENTEL_PERU_APP.getCode());
            result.setMessage(EnumCodeProcessTests.SIGN_IN_ERROR_ENTEL_PERU_APP.getDescription());
            result.setErrorMessage(e.getMessage());

            TestUtils.saveScreenshot(driver);
        } finally {
            if (result.isError()){
                result.setMessage(EnumCodeProcessTests.SIGN_IN_ERROR_ENTEL_PERU_APP.getDescription());
            }
            result.addTime(System.currentTimeMillis() - initStartTimeGlobalTest);
            return result;
        }
    }


}
