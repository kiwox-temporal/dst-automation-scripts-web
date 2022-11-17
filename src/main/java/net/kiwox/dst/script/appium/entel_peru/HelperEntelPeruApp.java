package net.kiwox.dst.script.appium.entel_peru;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import java.time.Duration;
import java.util.ArrayList;

public class HelperEntelPeruApp {
    public static final long DEFAULT_WAIT_TIMEOUT_ENTEL_PERU = 5;
    public static final long DEFAULT_WAIT_TIMEOUT_SING_IN_ENTEL_PERU = 3;

    private static final String POPUP_CLOSE_X = "popup_x";
    private static final String POPUP_CLOSE_Y = "popup_y";

    public static void closeApp(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        Thread.sleep(2000);
        driver.terminateApp("com.entel.movil");
    }
    public static void closeWebApp(ChromeHttpDriver driver) throws InterruptedException {
        Thread.sleep(2000);
        driver.close();

    }

    public static void closeAppSignIn(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        Thread.sleep(7000);
        driver.terminateApp("com.entel.movil");

    }

    public static void closePromotionPopUp(AndroidDriver<AndroidElement> driver) throws InterruptedException {
        Dimension size = driver.manage().window().getSize();
        int y = (int) (size.height * 0.10);
        int x = size.width - 100;
        Thread.sleep(6000);
        TouchAction ts = new TouchAction(driver);
        ts.tap(new PointOption<>().withCoordinates(new Point(x, y)))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                //.release()
                .perform();

    }



    public static void closePromotionPopUp(AndroidDriver<AndroidElement> driver, Integer popupCloseX, Integer popupCloseY) throws InterruptedException {
        Dimension size = driver.manage().window().getSize();
        Thread.sleep(6000);
        TouchAction ts = new TouchAction(driver);
        ts.tap(new PointOption<>().withCoordinates(new Point(popupCloseX, popupCloseY)))
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                //.release()
                .perform();

    }

    public static TestResultDetailEntelApp addDetailOpenApp() {
        long starTime = System.currentTimeMillis();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-D000-A001")
                .setDescription("Abrir APP")
                .setDetail("Abrir APP")
                .setErrorDetected(false)
                .setTime(System.currentTimeMillis() - starTime);
    }

    public static TestResultDetailEntelApp addDetailOpenWebApp() {
        long starTime = System.currentTimeMillis();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new TestResultDetailEntelApp().setCode("DST-ENTEL-PERU-WEB-D000-A001")
                .setDescription("Abrir navegador web")
                .setDetail("Abrir navegador web")
                .setErrorDetected(false)
                .setTime(System.currentTimeMillis() - starTime);
    }

    public static TestResultDetailEntelApp addErrorDetailItem(String errorCode, String detail, String description, long errorInitTime) {
        return new TestResultDetailEntelApp()
                .setCode(errorCode)
                .setDetail(detail)
                .setDescription(description)
                .setErrorDetected(true)
                .setTime(System.currentTimeMillis() - errorInitTime);
    }


    public static void switchTab(ChromeHttpDriver driver, int position){
        //Get the current window handle
        //String windowHandle = driver.getWindowHandle();
        //Get the list of window handles
        ArrayList tabs = new ArrayList(driver.getWindowHandles());
        System.out.println(tabs.size());

        //Use the list of window handles to switch between windows
        if (position < tabs.size()){
            driver.switchTo().window((String) tabs.get(position));
        }

        //Switch back to original window
        //driver.switchTo().window(mainWindowHandle);
    }




    public static Options buildOptionsPopup() {
        Options options = new Options();
        options.addOption(Option
                .builder(POPUP_CLOSE_X)
                .longOpt("popup_x")
                .hasArg()
                .required()
                .desc("Entel App Popup Point X (required)")
                .build());
        options.addOption(Option
                .builder(POPUP_CLOSE_Y)
                .longOpt("popup_y")
                .hasArg()
                .required()
                .desc("Entel App Popup Point Y (required)")
                .build());
        return options;
    }

}
