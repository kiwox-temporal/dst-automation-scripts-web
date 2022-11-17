package net.kiwox.dst.script.chrome.testflows;

import net.kiwox.dst.script.appium.TestUtils;
import net.kiwox.dst.script.chrome.IChromeTest;
import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.pojo.TestResult;
import net.kiwox.dst.script.pojo.TestResultDetailEntelApp;
import net.kiwox.dst.script.pojo.TestResultEntelApp;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;

import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.addErrorDetailItem;
import static net.kiwox.dst.script.appium.entel_peru.HelperEntelPeruApp.closeWebApp;

public class EntelWebVerifyPostPaidBagTest implements IChromeTest {

    private String phoneNumber;
    private String passCode;
    private TestResultEntelApp testResult;

    public static final Map<String, AbstractMap.SimpleEntry<String, String>> STEP_SLA_01;


    static {
        STEP_SLA_01 = Collections.singletonMap("Step01", new AbstractMap.SimpleEntry<>("DST-ENTEL_PERU-WEB-D002-E001", "Hacer clic a menú Bolsas"));
    }

    public EntelWebVerifyPostPaidBagTest(String phoneNumber, String code) {
        this.phoneNumber = phoneNumber;
        this.passCode = code;
        this.testResult = new TestResultEntelApp();
    }

    @Override
    public TestResult runTest(ChromeHttpDriver driver) throws InterruptedException {
        testResult = new EntelWebAuthenticationTest(phoneNumber, passCode)
                .automateStepsAuthentication(driver);
        verifyPostPaidBagWeb(driver);
        return testResult;

    }

    private void verifyPostPaidBagWeb(ChromeHttpDriver driver) throws InterruptedException {
        AbstractMap.SimpleEntry<String, String> errorStepOne = STEP_SLA_01.get("Step01");
        String currentStepErrorCode = errorStepOne.getKey(), currentStepDetail = errorStepOne.getValue();

        testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_SUCCESS_ENTEL_PERU_WEB.getCode());
        testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_SUCCESS_ENTEL_PERU_WEB.getDescription());
        long totalTimeOutAdd = System.currentTimeMillis(), currentTimeOut = System.currentTimeMillis();


        try {
            WebDriverWait wait = new WebDriverWait(driver, TestUtils.getWaitTimeout());


            String bagParentItemId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtBolsas_block_wtMenuItem']";
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(bagParentItemId))).click();
            TestUtils.saveScreenshotWebApp(driver);

            String firstBagItemPath = "//div[@class='Font_14 Text_Bold Text_Blue']";

            String prefixAmountText = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(firstBagItemPath)))
                    .getText();
            /*String bagItemId = "//*[@id='PE_Web_Personas_TH_wtPage_Wrapper_block_wtMenu_wtMenu_WB_RichWidgets_wtMisSaldos_block_wtMenuSubItems_wt188']";
            String amountText = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(bagItemId)))
                    .getText();*/
            TestUtils.saveScreenshotWebApp(driver);
            boolean expectedTextFound = prefixAmountText.trim()
                    .toLowerCase()
                    .contains("s/");
            String finalMessage = String.format("Texto \"s/{XXX} \" %s fue encontrado en la pantalla seleccionada", (expectedTextFound ? "si" : "no"));


            testResult.addItemDetail(new TestResultDetailEntelApp()
                    .setCode("DST-ENTEL_PERU-WEB-D001-002")
                    .setDescription(currentStepDetail)
                    .setDetail(currentStepDetail)
                    .setErrorDetected(!expectedTextFound)
                    .setTime(System.currentTimeMillis() - currentTimeOut));

            testResult.setError(!expectedTextFound);
            if (testResult.isError()) {
                testResult.setErrorMessage(finalMessage);
                testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BALANCE_ERROR_ENTEL_PERU_WEB.getCode());
            }
        } catch (Exception e) {
            testResult.addItemDetail(addErrorDetailItem(
                    currentStepErrorCode,
                    currentStepDetail,
                    e.getMessage(),
                    currentTimeOut)
            );
            testResult.setError(true);
            testResult.setCode(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_ERROR_ENTEL_PERU_WEB.getCode());
            testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_ERROR_ENTEL_PERU_WEB.getDescription());
            testResult.setErrorMessage(e.getMessage());
            TestUtils.saveScreenshotWebApp(driver);
        } finally {
            if (testResult.isError()) {
                testResult.setMessage(EnumCodeProcessTests.VERIFY_POST_PAID_BAG_ERROR_ENTEL_PERU_WEB.getDescription());
            }
            testResult.addTime(System.currentTimeMillis() - totalTimeOutAdd);
            closeWebApp(driver);
        }

    }

}
