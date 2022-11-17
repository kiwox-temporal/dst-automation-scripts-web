package net.kiwox.dst.script.appium;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import okhttp3.OkHttpClient;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    private static long waitTimeout;
    private static String screenshotFilePath;

    public static final String
            CLI_EXECUTE_AUTHENTICATION_00_ENTEL_WEB_APP = "web-app-entel-authenticacion-00",
            CLI_EXECUTE_VERIFY_POST_PAID_BALANCE_01_ENTEL_WEB_APP = "web-app-entel-verifcar-balance-post-pago-01",
            CLI_EXECUTE_VERIFY_POST_PAID_BAG_02_ENTEL_WEB_APP = "web-app-entel-verifcar-saldo-post-pago-02",

            CLI_EXECUTE_VERIFY_HISTORIC_RECEIPTS_POST_PAID_04_ENTEL_WEB_APP = "web-app-entel-verifcar-historico-recibos-post-pago-04";


    public static final String URL_ENDPOINT_DST = "https://web1-test.kiwox.cl/webdst/index.php/";

    private TestUtils() {
    }

    public static long getWaitTimeout() {
        return waitTimeout;
    }

    public static void setWaitTimeout(long waitTimeout) {
        TestUtils.waitTimeout = waitTimeout;
    }

    public static String getScreenshotFilePath() {
        return screenshotFilePath;
    }

    public static void setScreenshotFilePath(String screenshotFilePath) {
        TestUtils.screenshotFilePath = screenshotFilePath;
    }


    public static void saveScreenshot(RemoteWebDriver driver) {
        if (driver == null) {
            return;
        }

        File srcFile;
        try {
            srcFile = driver.getScreenshotAs(OutputType.FILE);
        } catch (WebDriverException e) {
            LOGGER.error("Error taking screenshot", e);
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Path targetPath = Paths.get(screenshotFilePath.replace("?????", dateFormat.format(new Date())));
        try {
            Files.copy(srcFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Screenshot saved [{}]", targetPath);
        } catch (IOException e) {
            LOGGER.error("Error saving screenshot", e);
        }
    }

    public static Retrofit getInstanceRetrofitClient() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_ENDPOINT_DST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        return retrofit;
    }

    public static void saveScreenshotWebApp(ChromeHttpDriver driver) {
        if (driver == null) {
            return;
        }

        File srcFile;
        try {
            srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        } catch (WebDriverException e) {
            LOGGER.error("Error taking screenshot", e);
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Path targetPath = Paths.get(screenshotFilePath.replace("?????", dateFormat.format(new Date())));
        try {
            Files.copy(srcFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Screenshot saved [{}]", targetPath);
        } catch (IOException e) {
            LOGGER.error("Error saving screenshot", e);
        }
    }

}
