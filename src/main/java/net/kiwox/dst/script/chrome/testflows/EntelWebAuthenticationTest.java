package net.kiwox.dst.script.chrome.testflows;

import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.custom.MultipleExpectedCondition;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import net.kiwox.dst.script.services.DstServiceApi;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.*;

public class EntelWebAuthenticationTest implements IChromeTest {

    private String phoneNumber;
    private String passCode;

    private DstServiceApi serviceApi;
    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEP_SLA_TEST;

    static {
        STEP_SLA_TEST = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL-PERU-WEB-D000-E001", "Ingresar código SMS"));
    }

    public EntelWebAuthenticationTest(String phoneNumber, String code) {
        this.phoneNumber = phoneNumber;
        this.passCode = code;
        this.serviceApi = TestUtils.getInstanceRetrofitClient()
                .create(DstServiceApi.class);
    }


    @Override
    public TestResultEntelApp runTest(ChromeHttpDriver driver) throws InterruptedException {
        TestResultEntelApp result = automateStepsAuthentication(driver);
        closeWebApp(driver);

        return result;
    }

    public TestResultEntelApp automateStepsAuthentication(ChromeHttpDriver driver) {
        AbstractMap.SimpleEntry<String, String> errorStep = STEP_SLA_TEST.get("Step01");
        String currentStepErrorCode = errorStep.getKey(), currentStepDetail = errorStep.getValue();

        TestResultEntelApp result = new TestResultEntelApp();
        List<TestResultDetailEntelApp> details = new ArrayList<>(Arrays.asList(addDetailOpenWebApp()));

        result.setCode(EnumCodeProcessTests.AUTHENTICATION_SUCCESS_ENTEL_PERU_WEB.getCode());
        result.setMessage(EnumCodeProcessTests.AUTHENTICATION_SUCCESS_ENTEL_PERU_WEB.getDescription());

        long finalTimeExecute = 0, currentStarTimeTaskExecute = System.currentTimeMillis();
        try {

            String oldUrlWeb = "https://miperfil.entel.pe/Login/";
            //String urlWeb = "https://miempresa.entel.pe/inicio/";
            driver.navigate().to(oldUrlWeb);
            WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());

            String companyInputId = "PE_Web_Login_Per_TH_wt38_block_wtMainContent_wt14_PE_Web_Login_Per_PATT_wtHomeAccessBlock_block_wtContent_PE_Web_Login_Per_PATT_wt65_block_wtInput_wtNumeroDesktop";
            String companySubmitId = "PE_Web_Login_Per_TH_wt38_block_wtMainContent_wt14_PE_Web_Login_Per_PATT_wtHomeAccessBlock_block_wtContent_PE_Web_Login_Per_PATT_wt65_block_wtButton_wtConfirmIngresoMovil";

            MultipleExpectedCondition loginCond = new MultipleExpectedCondition(By.id(companyInputId));
            Entry<Integer, WebElement> loginElement = wait.until(loginCond);
            if (loginElement.getKey() == 0) {
                WebElement companyInput = loginElement.getValue();
                companyInput.click();
                companyInput.sendKeys(phoneNumber);
                new Actions(driver).moveToElement(companyInput).moveByOffset(companyInput.getSize().getWidth(), 0).click().perform();
                TestUtils.saveScreenshotWebApp(driver);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(companySubmitId)))
                        .click();
            }

            String pathErrorCodeText = "//div[@id='PE_Web_Login_Per_TH_wt2_block_wtMainContent_wt7_PE_Web_Login_Per_PATT_wt33_block_wtContent_PE_Web_Login_Per_PATT_wtInputs_block_wtInputs_wtTextError'][@style='margin-left: 0px;margin-left: 0px;display:none;']";
            WebDriverWait waitErrorCode = new WebDriverWait(driver, 3);
            boolean errorCodeElementFind = false;


            long currenTimeInputPhoneNumber = System.currentTimeMillis() - currentStarTimeTaskExecute, currentTimeVerificationSecurityCode = 0;
            while (!errorCodeElementFind && !mainPageFind) {
                try {
                    waitErrorCode.until(ExpectedConditions.presenceOfElementLocated(By.xpath(pathErrorCodeText)));
                    errorCodeElementFind = true;
                } catch (Exception e) {
                    currentTimeVerificationSecurityCode = validateSecurityCodeAuth(waitErrorCode);
                }
            }

            details.add(new TestResultDetailEntelApp().setCode("DST-ENTEL_PERU-WEB-D000-001")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(false)
                    .setTime(currenTimeInputPhoneNumber + currentTimeVerificationSecurityCode)
            );
            result.setDetails(details);
            TestUtils.saveScreenshotWebApp(driver);
        } catch (Exception e) {
            result.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentStarTimeTaskExecute)
            );

            result.setError(true);
            result.setCode(EnumCodeProcessTests.AUTHENTICATION_ERROR_ENTEL_PERU_WEB.getCode());
            result.setMessage(EnumCodeProcessTests.AUTHENTICATION_ERROR_ENTEL_PERU_WEB.getDescription());
            result.setErrorMessage(e.getMessage());
            TestUtils.saveScreenshotWebApp(driver);
        } finally {
            if (result.isError()) {
                result.setMessage(EnumCodeProcessTests.SIGN_IN_ERROR_ENTEL_PERU_APP.getDescription());
            }
            finalTimeExecute = details.stream()
                    .mapToLong(TestResultDetailEntelApp::getTime)
                    .sum();
            result.addTime(finalTimeExecute);
            return result;
        }
    }

    boolean mainPageFind = false;

    private Long validateSecurityCodeAuth(WebDriverWait wait) throws IOException, InterruptedException {
        long currentStarTime = System.currentTimeMillis(); String verificationCode = "";
        long timePressOneDigit = 500;
        try {

            String pathInputs = "//*[@id='PE_Web_Login_Per_TH_wt2_block_wtMainContent_wt7_PE_Web_Login_Per_PATT_wt33_block_wtContent_PE_Web_Login_Per_PATT_wtInputs_block_wtInputs']/input";
            List<WebElement> inputElementsVerificationCode = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(pathInputs)));

            Thread.sleep(5000);
            Call<String> callVerificationCodeApi = serviceApi.requestGetVerificationCodePhone(
                    "rest/recuperarClave",
                    "rest",
                    phoneNumber,
                    "10"
            );
            Response<String> response = callVerificationCodeApi.execute();

            verificationCode = response.body();

            AtomicInteger index = new AtomicInteger(0);

            String finalVerificationCode = verificationCode;
            inputElementsVerificationCode.stream()
                    .forEach(e -> {
                        e.sendKeys(String.valueOf(finalVerificationCode.charAt(index.getAndIncrement())));
                        try {
                            Thread.sleep(timePressOneDigit);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        } catch (Exception e) {
            mainPageFind = true;
        } finally {
            return System.currentTimeMillis() - currentStarTime - (verificationCode.length() * timePressOneDigit);
        }


    }

}
